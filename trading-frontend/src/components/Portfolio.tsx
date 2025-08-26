'use client'

import { useEffect, useState } from 'react'
import { PortfolioChart } from './Charts'
import { TrendingUp, TrendingDown, Target, Lightbulb, BarChart3 } from 'lucide-react'
import axios from 'axios'

interface PortfolioProps {
  onLogout: () => void
}

export default function Portfolio({ onLogout }: PortfolioProps) {
  const [user, setUser] = useState<any>(null)
  const [portfolio, setPortfolio] = useState({
    totalValue: 0,
    totalPnL: 0,
    totalPnLPercent: 0,
    holdings: [] as any[]
  })
  const [orderHistory, setOrderHistory] = useState<any[]>([])
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
  const [projections, setProjections] = useState<any[]>([])

  const loadUserPortfolioData = async () => {
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
      }
    } catch (error) {
      console.error('Error loading user portfolio data:', error)
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
    
    loadUserPortfolioData()
    generateProjections()
    
    // Listen for portfolio updates from other components
    const handlePortfolioUpdate = () => {
      loadUserPortfolioData()
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

  // Recalculate portfolio when market data updates
  useEffect(() => {
    if (orderHistory.length > 0) {
      calculatePortfolio(orderHistory)
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
        const data = JSON.parse(savedData)
        setOrderHistory(data.orderHistory || [])
        calculatePortfolio(data.orderHistory || [])
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

  const generateProjections = () => {
    const scenarios = [
      {
        name: 'Conservative Growth',
        description: 'Diversified portfolio with blue-chip stocks',
        expectedReturn: 8,
        risk: 'Low',
        timeframe: '1 Year',
        color: 'text-green-400',
        suggestions: [
          'Increase MSFT position by 15%',
          'Add defensive stocks like JNJ',
          'Consider dividend-paying ETFs'
        ]
      },
      {
        name: 'Balanced Growth',
        description: 'Mix of growth and value stocks',
        expectedReturn: 12,
        risk: 'Medium',
        timeframe: '1 Year',
        color: 'text-blue-400',
        suggestions: [
          'Balance tech with healthcare',
          'Add emerging market exposure',
          'Consider sector rotation strategy'
        ]
      },
      {
        name: 'Aggressive Growth',
        description: 'High-growth tech and emerging sectors',
        expectedReturn: 18,
        risk: 'High',
        timeframe: '1 Year',
        color: 'text-orange-400',
        suggestions: [
          'Increase NVDA and TSLA positions',
          'Add AI and clean energy stocks',
          'Consider growth ETFs'
        ]
      }
    ]
    setProjections(scenarios)
  }

  const getPortfolioInsights = () => {
    const insights = []
    
    if (portfolio.holdings.length === 0) {
      insights.push({
        type: 'warning',
        title: 'No Holdings',
        message: 'Start building your portfolio by placing your first trade.',
        icon: Target
      })
    } else {
      if (portfolio.holdings.length < 3) {
        insights.push({
          type: 'info',
          title: 'Diversification Opportunity',
          message: 'Consider adding more positions to reduce risk through diversification.',
          icon: BarChart3
        })
      }
      
      if (portfolio.totalPnL > 0) {
        insights.push({
          type: 'success',
          title: 'Profitable Portfolio',
          message: `Your portfolio is up ${portfolio.totalPnLPercent.toFixed(2)}%. Consider taking some profits.`,
          icon: TrendingUp
        })
      } else if (portfolio.totalPnL < 0) {
        insights.push({
          type: 'warning',
          title: 'Portfolio Down',
          message: `Portfolio is down ${Math.abs(portfolio.totalPnLPercent).toFixed(2)}%. Review positions for rebalancing.`,
          icon: TrendingDown
        })
      }
    }
    
    return insights
  }

  return (
    <div className="p-6 space-y-6 bg-transparent">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-slate-100">Portfolio</h1>
        <div className="text-sm text-slate-400">
          Last updated: {new Date().toLocaleTimeString()}
        </div>
      </div>

      {/* Portfolio Overview */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div className="p-4 rounded-lg">
          <div className="text-sm text-slate-300">Total Value</div>
          <div className="text-3xl font-bold text-slate-100">${portfolio.totalValue.toFixed(2)}</div>
        </div>
        <div className="p-4 rounded-lg">
          <div className="text-sm text-slate-300">Total P&L</div>
          <div className={`text-3xl font-bold ${portfolio.totalPnL >= 0 ? 'text-green-400' : 'text-red-400'}`}>
            ${portfolio.totalPnL.toFixed(2)}
          </div>
        </div>
        <div className="p-4 rounded-lg">
          <div className="text-sm text-slate-300">P&L %</div>
          <div className={`text-3xl font-bold ${portfolio.totalPnLPercent >= 0 ? 'text-green-400' : 'text-red-400'}`}>
            {portfolio.totalPnLPercent.toFixed(2)}%
          </div>
        </div>
      </div>

      {/* Portfolio Insights */}
      <div className="p-4 rounded-lg">
        <h3 className="text-lg font-semibold text-slate-100 mb-4 flex items-center">
          <Lightbulb className="w-5 h-5 mr-2 text-yellow-400" />
          Portfolio Insights
        </h3>
        <div className="space-y-3">
          {getPortfolioInsights().map((insight, index) => {
            const Icon = insight.icon
            return (
              <div key={index} className={`p-3 rounded-lg border-l-4 ${
                insight.type === 'success' ? 'border-green-400 bg-green-900/20' :
                insight.type === 'warning' ? 'border-yellow-400 bg-yellow-900/20' :
                'border-blue-400 bg-blue-900/20'
              }`}>
                <div className="flex items-start space-x-3">
                  <Icon className={`w-5 h-5 mt-0.5 ${
                    insight.type === 'success' ? 'text-green-400' :
                    insight.type === 'warning' ? 'text-yellow-400' :
                    'text-blue-400'
                  }`} />
                  <div>
                    <div className="font-medium text-slate-100">{insight.title}</div>
                    <div className="text-sm text-slate-300">{insight.message}</div>
                  </div>
                </div>
              </div>
            )
          })}
        </div>
      </div>

      {/* Current Holdings */}
      <div className="p-4 rounded-lg">
        <h3 className="text-lg font-semibold text-slate-100 mb-4">Current Holdings</h3>
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
                  <th className="text-left p-2">P&L %</th>
                </tr>
              </thead>
              <tbody>
                {portfolio.holdings.map((holding, index) => {
                  const currentPrice = marketData[holding.symbol]?.price || holding.avgPrice
                  const pnlPercent = ((currentPrice - holding.avgPrice) / holding.avgPrice) * 100
                  return (
                    <tr key={index} className="text-slate-100 border-b border-slate-600">
                      <td className="p-2 font-medium">{holding.symbol}</td>
                      <td className="p-2">{holding.quantity}</td>
                      <td className="p-2">${holding.avgPrice.toFixed(2)}</td>
                      <td className="p-2">${currentPrice.toFixed(2)}</td>
                      <td className="p-2">${holding.currentValue.toFixed(2)}</td>
                      <td className={`p-2 ${holding.pnl >= 0 ? 'text-green-400' : 'text-red-400'}`}>
                        ${holding.pnl.toFixed(2)}
                      </td>
                      <td className={`p-2 ${pnlPercent >= 0 ? 'text-green-400' : 'text-red-400'}`}>
                        {pnlPercent.toFixed(2)}%
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="text-slate-300 text-center py-8">
            No holdings yet. Start trading to build your portfolio.
          </div>
        )}
      </div>

      {/* Portfolio Projections */}
      <div className="p-4 rounded-lg">
        <h3 className="text-lg font-semibold text-slate-100 mb-4">Portfolio Growth Projections</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {projections.map((projection, index) => (
            <div key={index} className="p-4 rounded-lg bg-slate-800/10 backdrop-blur-sm">
              <div className="flex items-center justify-between mb-3">
                <h4 className="font-medium text-slate-100">{projection.name}</h4>
                <span className={`text-sm px-2 py-1 rounded ${
                  projection.risk === 'Low' ? 'bg-green-900/30 text-green-400' :
                  projection.risk === 'Medium' ? 'bg-blue-900/30 text-blue-400' :
                  'bg-orange-900/30 text-orange-400'
                }`}>
                  {projection.risk} Risk
                </span>
              </div>
              <p className="text-sm text-slate-300 mb-3">{projection.description}</p>
              <div className="space-y-2">
                <div className="flex justify-between">
                  <span className="text-slate-400">Expected Return:</span>
                  <span className={projection.color}>+{projection.expectedReturn}%</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Timeframe:</span>
                  <span className="text-slate-100">{projection.timeframe}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Projected Value:</span>
                  <span className="text-white">
                    ${(portfolio.totalValue * (1 + projection.expectedReturn / 100)).toFixed(2)}
                  </span>
                </div>
              </div>
              <div className="mt-3 pt-3 border-t border-slate-700">
                <div className="text-xs text-slate-400 mb-2">Suggestions:</div>
                <ul className="text-xs text-slate-300 space-y-1">
                  {projection.suggestions.map((suggestion: string, idx: number) => (
                    <li key={idx}>• {suggestion}</li>
                  ))}
                </ul>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Portfolio Chart */}
      {portfolio.holdings.length > 0 && (
        <div className="p-4 rounded-lg">
          <h3 className="text-lg font-semibold text-slate-100 mb-4">Portfolio Allocation</h3>
          <PortfolioChart data={portfolio.holdings} />
        </div>
      )}
    </div>
  )
}
