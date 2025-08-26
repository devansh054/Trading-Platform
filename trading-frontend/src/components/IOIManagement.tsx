'use client'

import { useState, useEffect } from 'react'
import { Users, Plus, List, Building, User } from 'lucide-react'

interface IOI {
  id: string
  symbol: string
  side: string
  quantity: number
  price: number
  brokerId: string
  clientId: string
  createdAt: string
}

export default function IOIManagement() {
  const [activeIOIs, setActiveIOIs] = useState<IOI[]>([])
  const [result, setResult] = useState<string>('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    // Load user's IOIs on component mount
    loadUserIOIs()
  }, [])

  const loadUserIOIs = async () => {
    try {
      const token = localStorage.getItem('authToken')
      if (!token) return

      const response = await fetch('http://localhost:8080/api/ioi/active', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })

      if (response.ok) {
        const data = await response.json()
        setActiveIOIs(data)
      }
    } catch (error) {
      console.error('Error loading user IOIs:', error)
    }
  }

  const [formData, setFormData] = useState({
    symbol: 'MSFT',
    side: 'BUY',
    quantity: 500,
    price: 300.00,
    brokerId: 'BROKER001',
    clientId: 'CLIENT001'
  })

  const createIOI = async () => {
    setLoading(true)
    try {
      const token = localStorage.getItem('authToken')
      const response = await fetch('http://localhost:8080/api/ioi', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(formData)
      })

      if (response.ok) {
        const data = await response.json()
        setResult(`✅ IOI Created Successfully!\nIOI ID: ${data.id || data.ioiId}\nSymbol: ${data.symbol}\nSide: ${data.side}\nQuantity: ${data.quantity}\nPrice: $${data.price}`)
        // Automatically refresh IOIs to show the new one
        loadUserIOIs()
      } else {
        const errorData = await response.json().catch(() => ({}))
        setResult(`❌ Error: ${errorData.message || 'Failed to create IOI'}`)
      }
    } catch (error) {
      setResult(`❌ Network Error: ${error}`)
    }
    setLoading(false)
  }

  const getActiveIOIs = async () => {
    setLoading(true)
    await loadUserIOIs()
    setResult(`📋 Found ${activeIOIs.length} active IOIs`)
    setLoading(false)
  }

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center gap-3">
        <Users className="w-8 h-8 text-blue-400" />
        <h1 className="text-3xl font-bold text-white">IOI Management</h1>
      </div>
      
      <div className="text-slate-400">
        Create and manage Indications of Interest (IOI) for trading opportunities
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Create IOI Form */}
        <div className="bg-slate-800/50 rounded-lg p-6 space-y-6">
          <div className="flex items-center gap-2">
            <Plus className="w-5 h-5 text-green-400" />
            <h3 className="text-lg font-semibold text-white">Create IOI</h3>
          </div>
          <div className="text-slate-400 text-sm">
            Create a new Indication of Interest
          </div>
          
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Symbol</label>
                <input
                  type="text"
                  value={formData.symbol}
                  onChange={(e) => setFormData({...formData, symbol: e.target.value})}
                  placeholder="e.g., MSFT"
                  className="w-full p-3 bg-slate-900 border border-slate-600 rounded-md text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Side</label>
                <select
                  value={formData.side}
                  onChange={(e) => setFormData({...formData, side: e.target.value})}
                  className="w-full p-3 bg-slate-900 border border-slate-600 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="BUY">🟢 BUY</option>
                  <option value="SELL">🔴 SELL</option>
                </select>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Quantity</label>
                <input
                  type="number"
                  value={formData.quantity}
                  onChange={(e) => setFormData({...formData, quantity: parseInt(e.target.value)})}
                  className="w-full p-3 bg-slate-900 border border-slate-600 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">Price</label>
                <input
                  type="number"
                  step="0.01"
                  value={formData.price}
                  onChange={(e) => setFormData({...formData, price: parseFloat(e.target.value)})}
                  className="w-full p-3 bg-slate-900 border border-slate-600 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">
                  <Building className="w-4 h-4 inline mr-1" />
                  Broker ID
                </label>
                <input
                  type="text"
                  value={formData.brokerId}
                  onChange={(e) => setFormData({...formData, brokerId: e.target.value})}
                  className="w-full p-3 bg-slate-900 border border-slate-600 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-300 mb-2">
                  <User className="w-4 h-4 inline mr-1" />
                  Client ID
                </label>
                <input
                  type="text"
                  value={formData.clientId}
                  onChange={(e) => setFormData({...formData, clientId: e.target.value})}
                  className="w-full p-3 bg-slate-900 border border-slate-600 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div>

            <div className="flex gap-2">
              <button
                onClick={createIOI}
                disabled={loading}
                className="flex-1 py-3 px-4 bg-green-600 hover:bg-green-700 disabled:bg-slate-600 text-white rounded-md transition-colors font-medium flex items-center justify-center gap-2"
              >
                <Plus className="w-4 h-4" />
                {loading ? 'Creating...' : 'Create IOI'}
              </button>
              <button
                onClick={getActiveIOIs}
                disabled={loading}
                className="py-3 px-4 bg-slate-700 hover:bg-slate-600 disabled:bg-slate-600 text-white rounded-md transition-colors font-medium flex items-center gap-2"
              >
                <List className="w-4 h-4" />
                Active IOIs
              </button>
            </div>

            {result && (
              <div className={`p-4 rounded-lg font-mono text-sm whitespace-pre-line ${
                result.includes('✅') ? 'bg-green-900/20 border border-green-500/30 text-green-300' :
                result.includes('❌') ? 'bg-red-900/20 border border-red-500/30 text-red-300' :
                'bg-blue-900/20 border border-blue-500/30 text-blue-300'
              }`}>
                {result}
              </div>
            )}
          </div>
        </div>

        {/* Active IOIs List */}
        <div className="bg-slate-800/50 rounded-lg p-6 space-y-6">
          <div className="flex items-center gap-2">
            <List className="w-5 h-5 text-blue-400" />
            <h3 className="text-lg font-semibold text-white">Active IOIs</h3>
          </div>
          <div className="text-slate-400 text-sm">
            Current active Indications of Interest
          </div>
          
          <div>
            {activeIOIs.length > 0 ? (
              <div className="space-y-3">
                {activeIOIs.map((ioi) => (
                  <div key={ioi.id} className="p-4 bg-slate-900/50 rounded-lg border border-slate-700/50">
                    <div className="flex justify-between items-start">
                      <div>
                        <div className="font-semibold text-white">
                          {ioi.symbol} - {ioi.side}
                        </div>
                        <div className="text-sm text-slate-300">
                          Qty: {ioi.quantity} @ ${ioi.price}
                        </div>
                        <div className="text-xs text-slate-400">
                          Broker: {ioi.brokerId} | Client: {ioi.clientId}
                        </div>
                      </div>
                      <div className="text-xs text-slate-400">
                        {new Date(ioi.createdAt).toLocaleDateString()}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="text-center text-slate-400 py-8">
                No active IOIs found. Create your first IOI!
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
