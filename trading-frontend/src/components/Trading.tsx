'use client'

import { useState, useEffect } from 'react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { TrendingUp, TrendingDown } from 'lucide-react'
import axios from 'axios'

interface TradingProps {
  onLogout: () => void
}

export default function Trading({ onLogout }: TradingProps) {
  const [orderForm, setOrderForm] = useState({
    symbol: '',
    side: 'BUY',
    type: 'MARKET',
    quantity: '',
    price: ''
  })
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  
  // Available stocks for dropdown
  const availableStocks = [
    { symbol: 'AAPL', name: 'Apple Inc.' },
    { symbol: 'MSFT', name: 'Microsoft Corporation' },
    { symbol: 'GOOGL', name: 'Alphabet Inc.' },
    { symbol: 'TSLA', name: 'Tesla Inc.' },
    { symbol: 'NVDA', name: 'NVIDIA Corporation' },
    { symbol: 'AMZN', name: 'Amazon.com Inc.' },
    { symbol: 'META', name: 'Meta Platforms Inc.' },
    { symbol: 'NFLX', name: 'Netflix Inc.' },
    { symbol: 'AMD', name: 'Advanced Micro Devices' },
    { symbol: 'INTC', name: 'Intel Corporation' }
  ]
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
    // Update market data every 2 seconds for faster real-time feel
    const interval = setInterval(() => {
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
    }, 2000) // Update every 2 seconds instead of 5
    
    return () => clearInterval(interval)
  }, [])

  const handleOrderSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setMessage('')

    try {
      const token = localStorage.getItem('authToken')
      console.log('Auth token:', token)
      
      const orderData = {
        symbol: orderForm.symbol.toUpperCase(),
        side: orderForm.side,
        type: orderForm.type,
        quantity: parseInt(orderForm.quantity),
        price: orderForm.type === 'LIMIT' ? parseFloat(orderForm.price) : marketData[orderForm.symbol.toUpperCase()]?.price || 150,
        accountId: 'ACC001'
      }
      
      console.log('Order data:', orderData)

      const response = await axios.post('http://localhost:8080/api/orders', orderData, {
        headers: { Authorization: `Bearer ${token}` }
      })
      
      console.log('Order response:', response.data)

      if (response.data.orderId) {
        // Simulate realistic order statuses
        const orderStatus = Math.random() > 0.7 ? 'PENDING' : 'FILLED' // 30% chance of pending orders
        
        const newOrder = {
          ...orderData,
          orderId: response.data.orderId,
          timestamp: new Date().toISOString(),
          status: orderStatus
        }

        // Save to user's trading data
        const user = JSON.parse(localStorage.getItem('currentUser') || '{}')
        const userKey = `tradingData_${user.username}`
        const existingData = JSON.parse(localStorage.getItem(userKey) || '{"orderHistory": []}')
        existingData.orderHistory.push(newOrder)
        localStorage.setItem(userKey, JSON.stringify(existingData))

        setMessage(`Order ${response.data.orderId} placed successfully!`)
        setOrderForm({
          symbol: '',
          side: 'BUY',
          type: 'MARKET',
          quantity: '',
          price: ''
        })
        
        // Trigger a page refresh to update portfolio data across components
        window.dispatchEvent(new CustomEvent('portfolioUpdate'))
      }
    } catch (error: any) {
      setMessage(`Error: ${error.response?.data?.message || 'Failed to place order'}`)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="p-6 space-y-6 bg-transparent">
      <div className="p-6 space-y-6 bg-transparent">
      <h1 className="text-2xl font-bold text-slate-100">Trading</h1>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Order Form */}
        <div className="p-6 rounded-lg">
          <h3 className="text-lg font-semibold text-slate-100 mb-4">Place Order</h3>
          
          {message && (
          <div className={`p-3 rounded-md ${
            message.includes('Error') ? 'bg-red-900/50 text-red-300 border border-red-700/50' : 'bg-green-900/50 text-green-300 border border-green-700/50'
          }`}>
            {message}
          </div>
        )}

          <form onSubmit={handleOrderSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Symbol</label>
              <select 
                className="w-full px-3 py-2 border border-slate-600 rounded-md bg-slate-800/50 text-slate-100 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                value={orderForm.symbol}
                onChange={(e) => setOrderForm({ ...orderForm, symbol: e.target.value })}
                required
              >
                <option value="">Select a stock...</option>
                {availableStocks.map((stock) => (
                  <option key={stock.symbol} value={stock.symbol}>
                    {stock.symbol} - {stock.name}
                  </option>
                ))}
              </select>
            </div>
            
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Side</label>
                <select 
                  className="w-full px-3 py-2 border border-slate-600 rounded-md bg-slate-800/50 text-slate-100 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  value={orderForm.side}
                  onChange={(e) => setOrderForm({ ...orderForm, side: e.target.value })}
                >
                  <option value="BUY">BUY</option>
                  <option value="SELL">SELL</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Type</label>
                <select 
                  className="w-full px-3 py-2 border border-slate-600 rounded-md bg-slate-800/50 text-slate-100 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  value={orderForm.type}
                  onChange={(e) => setOrderForm({ ...orderForm, type: e.target.value })}
                >
                  <option value="MARKET">MARKET</option>
                  <option value="LIMIT">LIMIT</option>
                </select>
              </div>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-slate-300 mb-2">Quantity</label>
              <Input
                type="number"
                placeholder="100"
                value={orderForm.quantity}
                onChange={(e) => setOrderForm({ ...orderForm, quantity: e.target.value })}
                required
                className="mt-1"
              />
            </div>
            
            {orderForm.type === 'LIMIT' && (
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Price</label>
                <Input
                  type="number"
                  step="0.01"
                  placeholder="150.00"
                  value={orderForm.price}
                  onChange={(e) => setOrderForm({ ...orderForm, price: e.target.value })}
                  required
                  className="mt-1"
                />
              </div>
            )}
            
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? 'Placing Order...' : 'Place Order'}
            </Button>
          </form>
        </div>

        {/* Market Data */}
        <div className="p-6 rounded-lg">
          <h3 className="text-lg font-semibold text-slate-100 mb-4">Live Market Data</h3>
          <div className="text-xs text-slate-400 mb-3">Updates every 2 seconds</div>
          <div className="space-y-2">
            {availableStocks.map((stock) => {
              const data = marketData[stock.symbol]
              return (
                <div key={stock.symbol} className="flex justify-between items-center p-3 rounded-lg bg-slate-800/30 border border-slate-700/50">
                  <div>
                    <span className="font-medium text-slate-100">{stock.symbol}</span>
                    <div className="text-xs text-slate-400">{stock.name}</div>
                  </div>
                  <div className="text-right">
                    <div className="text-slate-100 font-semibold">${data?.price.toFixed(2) || '0.00'}</div>
                    <div className={`text-sm font-medium ${
                      (data?.change || 0) >= 0 ? 'text-green-400' : 'text-red-400'
                    }`}>
                      {(data?.change || 0) >= 0 ? '+' : ''}{data?.change.toFixed(2) || '0.00'}
                    </div>
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      </div>

      {/* Order Types Info */}
      <div className="p-6 rounded-lg">
        <h3 className="text-lg font-semibold text-white mb-4">Order Types</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="p-4 bg-slate-700 rounded">
            <h4 className="font-medium text-white mb-2">Market Order</h4>
            <p className="text-sm text-slate-300">
              Executes immediately at the current market price. Best for quick execution when price is less important than speed.
            </p>
          </div>
          <div className="p-4 bg-slate-700 rounded">
            <h4 className="font-medium text-white mb-2">Limit Order</h4>
            <p className="text-sm text-slate-300">
              Executes only at your specified price or better. Gives you price control but may not execute if the market doesn't reach your price.
            </p>
          </div>
        </div>
      </div>

      {/* Trading Tips */}
      <div className="p-6 rounded-lg">
        <h3 className="text-lg font-semibold text-white mb-4">Trading Tips</h3>
        <div className="space-y-2 text-sm text-slate-300">
          <div>• Always review your order details before submitting</div>
          <div>• Consider using limit orders in volatile markets</div>
          <div>• Monitor your positions regularly</div>
          <div>• Set stop-loss orders to manage risk</div>
          <div>• Never invest more than you can afford to lose</div>
        </div>
      </div>
    </div>
  )
}
