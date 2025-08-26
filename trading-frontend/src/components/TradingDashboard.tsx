'use client'

import { useState, useEffect } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { TrendingUp, TrendingDown, DollarSign, Activity, Users, BarChart3 } from 'lucide-react'
import axios from 'axios'

interface TradingDashboardProps {
  onLogout: () => void
  activeTab?: string
}

export function TradingDashboard({ onLogout, activeTab = 'dashboard' }: TradingDashboardProps) {
  const [user, setUser] = useState<any>(null)
  const [portfolioData, setPortfolioData] = useState({
    totalValue: 0,
    totalPnL: 0,
    totalPnLPercent: 0,
    holdings: {} as any
  })
  const [orderHistory, setOrderHistory] = useState<any[]>([])
  const [orderForm, setOrderForm] = useState({
    symbol: '',
    side: 'BUY',
    type: 'MARKET',
    quantity: '',
    price: ''
  })
  const [loading, setLoading] = useState(false)
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

  useEffect(() => {
    // Load user data from localStorage
    const userData = localStorage.getItem('currentUser')
    if (userData) {
      setUser(JSON.parse(userData))
    }
    
    loadPortfolioData()
    // loadOrderHistory() - function will be created when needed
    
    // Simulate real-time market data updates
    const interval = setInterval(() => {
      updateMarketData()
    }, 5000)
    
    return () => clearInterval(interval)
  }, [])

  const loadPortfolioData = () => {
    if (!user) return
    const userKey = `tradingData_${user.username}`
    const savedData = localStorage.getItem(userKey)
    
    if (savedData) {
      try {
        const userData = JSON.parse(savedData)
        setOrderHistory(userData.orderHistory || [])
        calculatePortfolio(userData.orderHistory || [])
      } catch (e) {
        console.error('Error loading user data:', e)
      }
    }
  }

  const saveUserData = (orders: any[]) => {
    const userKey = `tradingData_${user.username}`
    const userData = {
      orderHistory: orders,
      timestamp: new Date().toISOString()
    }
    localStorage.setItem(userKey, JSON.stringify(userData))
  }

  const calculatePortfolio = (orders: any[]) => {
    const holdings: any = {}
    let totalCost = 0

    orders.forEach(order => {
      if (order.status === 'FILLED') {
        const symbol = order.symbol
        const quantity = order.side === 'BUY' ? order.quantity : -order.quantity
        const price = order.price || order.avgPrice || 0

        if (!holdings[symbol]) {
          holdings[symbol] = { quantity: 0, totalCost: 0, avgPrice: 0 }
        }

        const holding = holdings[symbol]
        const oldQuantity = holding.quantity
        const newQuantity = oldQuantity + quantity

        if (newQuantity !== 0) {
          if (quantity > 0) { // Buy
            holding.totalCost += quantity * price
            holding.quantity = newQuantity
            holding.avgPrice = holding.totalCost / holding.quantity
          } else { // Sell
            const sellQuantity = Math.abs(quantity)
            if (oldQuantity > 0) {
              const costReduction = (sellQuantity / oldQuantity) * holding.totalCost
              holding.totalCost -= costReduction
              holding.quantity = newQuantity
              if (holding.quantity > 0) {
                holding.avgPrice = holding.totalCost / holding.quantity
              }
            }
          }
        } else {
          holding.quantity = 0
          holding.totalCost = 0
          holding.avgPrice = 0
        }
      }
    })

    // Remove zero positions
    Object.keys(holdings).forEach(symbol => {
      if (holdings[symbol].quantity === 0) {
        delete holdings[symbol]
      } else {
        totalCost += holdings[symbol].totalCost
      }
    })

    // Calculate total value and P&L
    let totalValue = 0
    Object.entries(holdings).forEach(([symbol, holding]: [string, any]) => {
      const currentPrice = marketData[symbol]?.price || holding.avgPrice
      const marketValue = holding.quantity * currentPrice
      totalValue += marketValue
    })

    const totalPnL = totalValue - totalCost
    const totalPnLPercent = totalCost > 0 ? (totalPnL / totalCost) * 100 : 0

    setPortfolioData({
      totalValue,
      totalPnL,
      totalPnLPercent,
      holdings
    })
  }

  const updateMarketData = () => {
    setMarketData(prev => {
      const updated = { ...prev }
      Object.keys(updated).forEach(symbol => {
        const changePercent = (Math.random() - 0.5) * 0.02 // ±1%
        const change = updated[symbol].price * changePercent
        updated[symbol].price += change
        updated[symbol].change = change
      })
      return updated
    })
  }

  const handleOrderSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)

    try {
      const token = localStorage.getItem('authToken')
      const orderData = {
        symbol: orderForm.symbol.toUpperCase(),
        side: orderForm.side,
        type: orderForm.type,
        quantity: parseInt(orderForm.quantity),
        price: orderForm.type === 'LIMIT' ? parseFloat(orderForm.price) : marketData[orderForm.symbol.toUpperCase()]?.price || 150,
        accountId: 'ACC001'
      }

      const response = await axios.post('http://localhost:8080/api/orders', orderData, {
        headers: { Authorization: `Bearer ${token}` }
      })

      if (response.data.orderId) {
        // Simulate realistic order statuses
        const orderStatus = Math.random() > 0.7 ? 'PENDING' : 'FILLED' // 30% chance of pending orders
        
        const newOrder = {
          ...orderData,
          orderId: response.data.orderId,
          timestamp: new Date().toISOString(),
          status: orderStatus,
          avgPrice: orderData.price
        }

        const updatedOrders = [newOrder, ...orderHistory]
        setOrderHistory(updatedOrders)
        saveUserData(updatedOrders)
        calculatePortfolio(updatedOrders)

        setOrderForm({
          symbol: '',
          side: 'BUY',
          type: 'MARKET',
          quantity: '',
          price: ''
        })
      }
    } catch (error) {
      console.error('Order creation failed:', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-background">
      {/* Header */}
      <header className="border-b bg-card">
        <div className="container mx-auto px-4 py-4 flex justify-between items-center">
          <h1 className="text-2xl font-bold">Trading Dashboard</h1>
          <div className="flex items-center gap-4">
            <span className="text-sm text-muted-foreground">Welcome, {user?.username || 'User'}</span>
            <Button variant="outline" onClick={onLogout}>Logout</Button>
          </div>
        </div>
      </header>

      <div className="container mx-auto px-4 py-6">
        {/* Portfolio Overview Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Portfolio Value</CardTitle>
              <DollarSign className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">${portfolioData.totalValue.toLocaleString('en-US', { minimumFractionDigits: 2 })}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">P&L</CardTitle>
              {portfolioData.totalPnL >= 0 ? 
                <TrendingUp className="h-4 w-4 text-green-600" /> : 
                <TrendingDown className="h-4 w-4 text-red-600" />
              }
            </CardHeader>
            <CardContent>
              <div className={`text-2xl font-bold ${portfolioData.totalPnL >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                {portfolioData.totalPnL >= 0 ? '+' : ''}${portfolioData.totalPnL.toFixed(2)}
              </div>
              <p className={`text-xs ${portfolioData.totalPnL >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                {portfolioData.totalPnLPercent >= 0 ? '+' : ''}{portfolioData.totalPnLPercent.toFixed(2)}%
              </p>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Total Orders</CardTitle>
              <Activity className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{orderHistory.length}</div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Holdings</CardTitle>
              <BarChart3 className="h-4 w-4 text-muted-foreground" />
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{Object.keys(portfolioData.holdings).length}</div>
            </CardContent>
          </Card>
        </div>

        {/* Main Content */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Order Form */}
          <Card>
            <CardHeader>
              <CardTitle>Place Order</CardTitle>
              <CardDescription>Create a new trading order</CardDescription>
            </CardHeader>
            <CardContent>
              <form onSubmit={handleOrderSubmit} className="space-y-4">
                <div>
                  <label className="text-sm font-medium">Symbol</label>
                  <Input
                    placeholder="AAPL"
                    value={orderForm.symbol}
                    onChange={(e) => setOrderForm({ ...orderForm, symbol: e.target.value })}
                    required
                  />
                </div>
                
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-sm font-medium">Side</label>
                    <select 
                      className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                      value={orderForm.side}
                      onChange={(e) => setOrderForm({ ...orderForm, side: e.target.value })}
                    >
                      <option value="BUY">BUY</option>
                      <option value="SELL">SELL</option>
                    </select>
                  </div>
                  <div>
                    <label className="text-sm font-medium">Type</label>
                    <select 
                      className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
                      value={orderForm.type}
                      onChange={(e) => setOrderForm({ ...orderForm, type: e.target.value })}
                    >
                      <option value="MARKET">MARKET</option>
                      <option value="LIMIT">LIMIT</option>
                    </select>
                  </div>
                </div>
                
                <div>
                  <label className="text-sm font-medium">Quantity</label>
                  <Input
                    type="number"
                    placeholder="100"
                    value={orderForm.quantity}
                    onChange={(e) => setOrderForm({ ...orderForm, quantity: e.target.value })}
                    required
                  />
                </div>
                
                {orderForm.type === 'LIMIT' && (
                  <div>
                    <label className="text-sm font-medium">Price</label>
                    <Input
                      type="number"
                      step="0.01"
                      placeholder="150.00"
                      value={orderForm.price}
                      onChange={(e) => setOrderForm({ ...orderForm, price: e.target.value })}
                      required
                    />
                  </div>
                )}
                
                <Button type="submit" className="w-full" disabled={loading}>
                  {loading ? 'Placing Order...' : 'Place Order'}
                </Button>
              </form>
            </CardContent>
          </Card>

          {/* Portfolio Holdings */}
          <div className="lg:col-span-2">
            <Card>
              <CardHeader>
                <CardTitle>Portfolio Holdings</CardTitle>
                <CardDescription>Your current positions</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead>
                      <tr className="border-b">
                        <th className="text-left p-2">Symbol</th>
                        <th className="text-left p-2">Shares</th>
                        <th className="text-left p-2">Avg Cost</th>
                        <th className="text-left p-2">Current</th>
                        <th className="text-left p-2">Market Value</th>
                        <th className="text-left p-2">P&L</th>
                        <th className="text-left p-2">%</th>
                      </tr>
                    </thead>
                    <tbody>
                      {Object.entries(portfolioData.holdings).map(([symbol, holding]: [string, any]) => {
                        const currentPrice = marketData[symbol]?.price || holding.avgPrice
                        const marketValue = holding.quantity * currentPrice
                        const pnl = marketValue - holding.totalCost
                        const pnlPercent = holding.totalCost > 0 ? (pnl / holding.totalCost) * 100 : 0
                        
                        return (
                          <tr key={symbol} className="border-b">
                            <td className="p-2 font-medium">{symbol}</td>
                            <td className="p-2">{holding.quantity.toLocaleString()}</td>
                            <td className="p-2">${holding.avgPrice.toFixed(2)}</td>
                            <td className="p-2">${currentPrice.toFixed(2)}</td>
                            <td className="p-2">${marketValue.toLocaleString('en-US', { minimumFractionDigits: 2 })}</td>
                            <td className={`p-2 ${pnl >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                              ${pnl.toFixed(2)}
                            </td>
                            <td className={`p-2 ${pnl >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                              {pnlPercent.toFixed(2)}%
                            </td>
                          </tr>
                        )
                      })}
                    </tbody>
                  </table>
                  {Object.keys(portfolioData.holdings).length === 0 && (
                    <div className="text-center py-8 text-muted-foreground">
                      No holdings yet. Place your first order to get started.
                    </div>
                  )}
                </div>
              </CardContent>
            </Card>
          </div>
        </div>

        {/* Order History */}
        <Card className="mt-6">
          <CardHeader>
            <CardTitle>Recent Orders</CardTitle>
            <CardDescription>Your trading history</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="space-y-2">
              {orderHistory.slice(0, 10).map((order, index) => (
                <div key={order.orderId || index} className="flex justify-between items-center p-3 border rounded">
                  <div className="flex items-center gap-4">
                    <span className="font-medium">{order.symbol}</span>
                    <span className={`px-2 py-1 rounded text-xs ${order.side === 'BUY' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                      {order.side}
                    </span>
                    <span className="text-sm text-muted-foreground">{order.quantity} shares</span>
                  </div>
                  <div className="text-right">
                    <div className="font-medium">${(order.price || order.avgPrice || 0).toFixed(2)}</div>
                    <div className="text-xs text-muted-foreground">{order.status}</div>
                  </div>
                </div>
              ))}
              {orderHistory.length === 0 && (
                <div className="text-center py-8 text-muted-foreground">
                  No orders yet. Place your first order to see it here.
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
