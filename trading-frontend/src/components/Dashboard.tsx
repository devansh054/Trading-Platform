'use client'

import { useEffect, useState } from 'react'
import { PortfolioChart, MarketChart } from './Charts'
import axios from 'axios'

interface DashboardProps {
  onLogout: () => void
}

export default function Dashboard({ onLogout }: DashboardProps) {
  const [user, setUser] = useState<any>(null)
  const [portfolio, setPortfolio] = useState({
    totalValue: 0,
    totalPnL: 0,
    totalPnLPercent: 0,
    holdings: [] as any[]
  })
  const [orderHistory, setOrderHistory] = useState<any[]>([])
  const [ongoingOrders, setOngoingOrders] = useState<any[]>([])
  const [marketData, setMarketData] = useState<{[key: string]: {price: number, change: number}}>({
    AAPL: { price: 227.52, change: 1.25 },
    MSFT: { price: 420.50, change: -2.15 },
    GOOGL: { price: 164.85, change: 2.30 },
    TSLA: { price: 245.80, change: -5.20 },
    NVDA: { price: 125.61, change: 3.60 },
    AMZN: { price: 227.94, change: 4.50 },
    META: { price: 485.30, change: -8.75 },
    NFLX: { price: 520.15, change: 12.40 },
    AMD: { price: 165.80, change: 3.20 },
    INTC: { price: 45.90, change: -1.15 }
  })

  const loadUserData = async () => {
    try {
      const token = localStorage.getItem('authToken')
      if (!token) return

      // Fetch user's orders from database
      const ordersResponse = await axios.get('http://localhost:8080/api/orders', {
        headers: { Authorization: `Bearer ${token}` }
      })
      
      if (ordersResponse.data) {
        setOrderHistory(ordersResponse.data)
        calculatePortfolio(ordersResponse.data)
        
        // Filter ongoing orders
        const ongoing = ordersResponse.data.filter((order: any) => 
          order.status === 'PENDING' || order.status === 'PARTIAL' || order.status === 'NEW'
        )
        setOngoingOrders(ongoing)
      }
    } catch (error) {
      console.error('Error loading user data:', error)
      // Fallback to localStorage if API fails
      loadPortfolioData()
    }
  }

  useEffect(() => {
    // Load user data from localStorage
    const userData = localStorage.getItem('currentUser')
    if (userData) {
      setUser(JSON.parse(userData))
    }
    
    // Load user-specific data from database
    loadUserData()
    
    // Listen for portfolio updates from other components
    const handlePortfolioUpdate = () => {
      loadUserData()
    }
    
    window.addEventListener('portfolioUpdate', handlePortfolioUpdate)
    
    // Simulate real-time market data updates
    const interval = setInterval(() => {
      updateMarketData()
    }, 2000)
    
    return () => {
      clearInterval(interval)
      window.removeEventListener('portfolioUpdate', handlePortfolioUpdate)
    }
  }, [])

  // Load portfolio data when user is set
  useEffect(() => {
    if (user) {
      loadPortfolioData()
    }
  }, [user])

  // Recalculate portfolio when market data updates
  useEffect(() => {
    if (orderHistory.length > 0) {
      calculatePortfolio(orderHistory)
      
      // Simulate order status changes over time (20% chance pending orders get filled)
      const updatedOrders = orderHistory.map(order => {
        if (order.status === 'PENDING' && Math.random() > 0.8) {
          return { ...order, status: 'FILLED' }
        }
        return order
      })
      
      if (JSON.stringify(updatedOrders) !== JSON.stringify(orderHistory)) {
        setOrderHistory(updatedOrders)
        const userData = localStorage.getItem('currentUser')
        if (userData) {
          const user = JSON.parse(userData)
          const userKey = `tradingData_${user.username}`
          const existingData = JSON.parse(localStorage.getItem(userKey) || '{"orderHistory": []}')
          existingData.orderHistory = updatedOrders
          localStorage.setItem(userKey, JSON.stringify(existingData))
        }
        
        // Update ongoing orders after status changes
        const ongoing = updatedOrders.filter((order: any) => 
          order.status === 'PENDING' || order.status === 'PARTIAL' || order.status === 'NEW'
        )
        setOngoingOrders(ongoing)
      }
    }
  }, [marketData, orderHistory])

  const loadPortfolioData = () => {
    const userData = localStorage.getItem('currentUser')
    if (!userData) return
    
    const user = JSON.parse(userData)
    const userKey = `tradingData_${user.username}`
    const savedData = localStorage.getItem(userKey)
    
    if (savedData) {
      try {
        const userData = JSON.parse(savedData)
        setOrderHistory(userData.orderHistory || [])
        calculatePortfolio(userData.orderHistory || [])
        
        // Filter ongoing orders (assuming orders with status 'PENDING' or 'PARTIAL')
        const ongoing = (userData.orderHistory || []).filter((order: any) => 
          order.status === 'PENDING' || order.status === 'PARTIAL' || order.status === 'NEW'
        )
        setOngoingOrders(ongoing)
      } catch (e) {
        console.error('Error loading user data:', e)
      }
    }
  }

  const calculatePortfolio = (orders: any[]) => {
    const holdings: {[key: string]: {quantity: number, avgPrice: number, currentValue: number, pnl: number}} = {}
    let totalValue = 0
    let totalPnL = 0

    orders.forEach(order => {
      if (order.status === 'FILLED') {
        const symbol = order.symbol
        const quantity = order.side === 'BUY' ? order.quantity : -order.quantity
        const price = order.price

        if (!holdings[symbol]) {
          holdings[symbol] = { quantity: 0, avgPrice: 0, currentValue: 0, pnl: 0 }
        }

        const currentHolding = holdings[symbol]
        const newQuantity = currentHolding.quantity + quantity

        if (newQuantity !== 0) {
          currentHolding.avgPrice = ((currentHolding.avgPrice * currentHolding.quantity) + (price * quantity)) / newQuantity
        }
        currentHolding.quantity = newQuantity

        const currentPrice = marketData[symbol]?.price || price
        currentHolding.currentValue = currentHolding.quantity * currentPrice
        currentHolding.pnl = (currentPrice - currentHolding.avgPrice) * currentHolding.quantity

        totalValue += currentHolding.currentValue
        totalPnL += currentHolding.pnl
      }
    })

    const holdingsArray = Object.entries(holdings)
      .filter(([_, holding]) => holding.quantity !== 0)
      .map(([symbol, holding]) => ({ symbol, ...holding }))

    const totalPnLPercent = totalValue > 0 ? (totalPnL / (totalValue - totalPnL)) * 100 : 0

    setPortfolio({
      totalValue,
      totalPnL,
      totalPnLPercent,
      holdings: holdingsArray
    })
  }

  const updateMarketData = () => {
    setMarketData(prev => {
      const updated = { ...prev }
      Object.keys(updated).forEach(symbol => {
        const change = (Math.random() - 0.5) * 15 // Larger price movements
        updated[symbol] = {
          price: Math.max(updated[symbol].price + change, 1),
          change: change
        }
      })
      return updated
    })
  }

  return (
    <div className="p-6 space-y-6 bg-transparent">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-slate-100">Dashboard</h1>
        <div className="text-sm text-slate-400">
          Welcome back, {user?.username || 'User'}
        </div>
      </div>

      {/* Portfolio Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="p-4 rounded-lg">
          <div className="text-sm text-slate-300">Portfolio Value</div>
          <div className="text-2xl font-bold text-slate-100">${portfolio.totalValue.toFixed(2)}</div>
        </div>
        <div className="p-4 rounded-lg">
          <div className="text-sm text-slate-300">Total P&L</div>
          <div className={`text-2xl font-bold ${portfolio.totalPnL >= 0 ? 'text-green-400' : 'text-red-400'}`}>
            ${portfolio.totalPnL.toFixed(2)}
          </div>
        </div>
        <div className="p-4 rounded-lg">
          <div className="text-sm text-slate-300">P&L %</div>
          <div className={`text-2xl font-bold ${portfolio.totalPnLPercent >= 0 ? 'text-green-400' : 'text-red-400'}`}>
            {portfolio.totalPnLPercent.toFixed(2)}%
          </div>
        </div>
        <div className="p-4 rounded-lg">
          <div className="text-sm text-slate-300">Active Orders</div>
          <div className="text-2xl font-bold text-slate-100">{ongoingOrders.length}</div>
        </div>
      </div>

      {/* Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="p-4 rounded-lg">
          <h3 className="text-lg font-semibold text-slate-100 mb-4">Portfolio Performance</h3>
          <PortfolioChart data={portfolio.holdings} />
        </div>
        <div className="p-4 rounded-lg">
          <h3 className="text-lg font-semibold text-slate-100 mb-4">Market Data</h3>
          <MarketChart data={marketData} />
        </div>
      </div>

      {/* Ongoing Orders */}
      <div className="p-4 rounded-lg">
        <h3 className="text-lg font-semibold text-slate-100 mb-4">Ongoing Orders</h3>
        {ongoingOrders.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-slate-300 border-b border-slate-600">
                  <th className="text-left p-2">Order ID</th>
                  <th className="text-left p-2">Symbol</th>
                  <th className="text-left p-2">Side</th>
                  <th className="text-left p-2">Type</th>
                  <th className="text-left p-2">Quantity</th>
                  <th className="text-left p-2">Price</th>
                  <th className="text-left p-2">Status</th>
                  <th className="text-left p-2">Time</th>
                </tr>
              </thead>
              <tbody>
                {ongoingOrders.map((order, index) => (
                  <tr key={index} className="text-slate-100 border-b border-slate-600">
                    <td className="p-2">{order.orderId}</td>
                    <td className="p-2">{order.symbol}</td>
                    <td className={`p-2 ${order.side === 'BUY' ? 'text-green-400' : 'text-red-400'}`}>
                      {order.side}
                    </td>
                    <td className="p-2">{order.type}</td>
                    <td className="p-2">{order.quantity}</td>
                    <td className="p-2">${order.price?.toFixed(2)}</td>
                    <td className="p-2">
                      <span className="px-2 py-1 rounded text-xs bg-yellow-600 text-yellow-100">
                        {order.status}
                      </span>
                    </td>
                    <td className="p-2">{new Date(order.timestamp).toLocaleTimeString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="text-slate-300 text-center py-8">No ongoing orders</div>
        )}
      </div>

      {/* Order History */}
      <div className="p-4 rounded-lg">
        <h3 className="text-lg font-semibold text-slate-100 mb-4">Recent Order History</h3>
        {orderHistory.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-slate-300 border-b border-slate-600">
                  <th className="text-left p-2">Order ID</th>
                  <th className="text-left p-2">Symbol</th>
                  <th className="text-left p-2">Side</th>
                  <th className="text-left p-2">Type</th>
                  <th className="text-left p-2">Quantity</th>
                  <th className="text-left p-2">Price</th>
                  <th className="text-left p-2">Status</th>
                  <th className="text-left p-2">Time</th>
                </tr>
              </thead>
              <tbody>
                {orderHistory.slice(-10).reverse().map((order, index) => (
                  <tr key={index} className="text-slate-100 border-b border-slate-600">
                    <td className="p-2">{order.orderId}</td>
                    <td className="p-2">{order.symbol}</td>
                    <td className={`p-2 ${order.side === 'BUY' ? 'text-green-400' : 'text-red-400'}`}>
                      {order.side}
                    </td>
                    <td className="p-2">{order.type}</td>
                    <td className="p-2">{order.quantity}</td>
                    <td className="p-2">${order.price?.toFixed(2)}</td>
                    <td className="p-2">
                      <span className={`px-2 py-1 rounded text-xs ${
                        order.status === 'FILLED' ? 'bg-green-600 text-green-100' :
                        order.status === 'CANCELLED' ? 'bg-red-600 text-red-100' :
                        'bg-yellow-600 text-yellow-100'
                      }`}>
                        {order.status}
                      </span>
                    </td>
                    <td className="p-2">{new Date(order.timestamp).toLocaleTimeString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="text-slate-300 text-center py-8">No order history</div>
        )}
      </div>

      {/* Portfolio Holdings */}
      <div className="p-4 rounded-lg">
        <h3 className="text-lg font-semibold text-slate-100 mb-4">Portfolio Holdings</h3>
        {portfolio.holdings.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-slate-300 border-b border-slate-600">
                  <th className="text-left p-2">Symbol</th>
                  <th className="text-left p-2">Quantity</th>
                  <th className="text-left p-2">Avg Price</th>
                  <th className="text-left p-2">Current Price</th>
                  <th className="text-left p-2">Market Value</th>
                  <th className="text-left p-2">P&L</th>
                </tr>
              </thead>
              <tbody>
                {portfolio.holdings.map((holding, index) => (
                  <tr key={index} className="text-slate-100 border-b border-slate-600">
                    <td className="p-2 font-medium">{holding.symbol}</td>
                    <td className="p-2">{holding.quantity}</td>
                    <td className="p-2">${holding.avgPrice.toFixed(2)}</td>
                    <td className="p-2">${marketData[holding.symbol]?.price.toFixed(2)}</td>
                    <td className="p-2">${holding.currentValue.toFixed(2)}</td>
                    <td className={`p-2 ${holding.pnl >= 0 ? 'text-green-400' : 'text-red-400'}`}>
                      ${holding.pnl.toFixed(2)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="text-slate-300 text-center py-8">No holdings</div>
        )}
      </div>
    </div>
  )
}
