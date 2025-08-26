'use client'

import { useState } from 'react'
import { ArrowLeftRight, Upload, Download, FileText, CheckCircle, XCircle, Clock } from 'lucide-react'

export default function TradeMessages() {
  const [activeTab, setActiveTab] = useState('process')
  const [xmlContent, setXmlContent] = useState('')
  const [result, setResult] = useState<any>(null)
  const [loading, setLoading] = useState(false)
  const [messageHistory, setMessageHistory] = useState<any[]>([])

  const sampleTradeMessage = `<?xml version="1.0" encoding="UTF-8"?>
<FIXML xmlns="http://www.fixprotocol.org/FIXML-5-0-SP2">
  <NewOrderSingle>
    <ClOrdID>ORD-12345-001</ClOrdID>
    <Symbol>AAPL</Symbol>
    <Side>1</Side>
    <TransactTime>2024-01-15T10:30:00Z</TransactTime>
    <OrderQty>100</OrderQty>
    <OrdType>2</OrdType>
    <Price>150.25</Price>
    <TimeInForce>0</TimeInForce>
    <Account>ACC-001</Account>
  </NewOrderSingle>
</FIXML>`

  const loadSampleMessage = () => {
    setXmlContent(sampleTradeMessage)
  }

  const processTradeMessage = async () => {
    if (!xmlContent.trim()) {
      setResult({ error: 'Please enter XML content to process' })
      return
    }

    setLoading(true)
    try {
      const token = localStorage.getItem('authToken')
      const response = await fetch('http://localhost:8080/api/xml/parse', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ xmlContent })
      })

      const data = await response.json()
      
      if (response.ok) {
        setResult({
          success: true,
          messageType: data.messageType,
          parsedData: data.parsedData,
          fieldCount: data.fieldCount,
          timestamp: new Date().toISOString()
        })
        
        // Add to message history
        const newMessage = {
          id: Date.now(),
          type: data.messageType,
          status: 'PROCESSED',
          timestamp: new Date().toISOString(),
          content: xmlContent.substring(0, 100) + '...'
        }
        setMessageHistory(prev => [newMessage, ...prev.slice(0, 9)])
      } else {
        setResult({ error: data.message || 'Failed to process trade message' })
      }
    } catch (error) {
      setResult({ error: 'Network error: Unable to connect to server' })
    } finally {
      setLoading(false)
    }
  }

  const validateTradeMessage = async () => {
    if (!xmlContent.trim()) {
      setResult({ error: 'Please enter XML content to validate' })
      return
    }

    setLoading(true)
    try {
      const token = localStorage.getItem('authToken')
      const response = await fetch('http://localhost:8080/api/xml/validate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ 
          xmlContent,
          messageType: 'NewOrderSingle'
        })
      })

      const data = await response.json()
      
      if (response.ok) {
        setResult({
          success: true,
          isValid: data.isValid,
          messageType: data.messageType,
          validationDetails: data.validationDetails,
          errors: data.errors || [],
          timestamp: new Date().toISOString()
        })
      } else {
        setResult({ error: data.message || 'Failed to validate trade message' })
      }
    } catch (error) {
      setResult({ error: 'Network error: Unable to connect to server' })
    } finally {
      setLoading(false)
    }
  }

  const tabs = [
    { id: 'process', label: 'Process Message', icon: ArrowLeftRight },
    { id: 'validate', label: 'Validate Message', icon: CheckCircle },
    { id: 'history', label: 'Message History', icon: Clock }
  ]

  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center gap-3">
        <ArrowLeftRight className="w-8 h-8 text-blue-400" />
        <h1 className="text-3xl font-bold text-white">Trade Messages</h1>
      </div>
      
      <div className="text-slate-400">
        Process and validate FIX protocol trade messages in XML format
      </div>

      {/* Tab Navigation */}
      <div className="flex space-x-1 bg-slate-800/50 p-1 rounded-lg">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`flex items-center gap-2 px-4 py-2 rounded-md transition-all ${
              activeTab === tab.id
                ? 'bg-blue-600 text-white'
                : 'text-slate-400 hover:text-white hover:bg-slate-700/50'
            }`}
          >
            <tab.icon className="w-4 h-4" />
            {tab.label}
          </button>
        ))}
      </div>

      {/* Process Message Tab */}
      {activeTab === 'process' && (
        <div className="space-y-6">
          <div className="bg-slate-800/50 rounded-lg p-6 space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-white">XML Trade Message</h3>
              <button
                onClick={loadSampleMessage}
                className="px-4 py-2 bg-slate-700 hover:bg-slate-600 text-white rounded-md transition-colors"
              >
                Load Sample
              </button>
            </div>
            
            <textarea
              value={xmlContent}
              onChange={(e) => setXmlContent(e.target.value)}
              placeholder="Paste your FIX XML trade message here..."
              className="w-full h-64 p-4 bg-slate-900 border border-slate-600 rounded-md text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent font-mono text-sm"
            />
            
            <button
              onClick={processTradeMessage}
              disabled={loading}
              className="w-full py-3 bg-blue-600 hover:bg-blue-700 disabled:bg-slate-600 text-white rounded-md transition-colors font-medium"
            >
              {loading ? 'Processing...' : 'Process Trade Message'}
            </button>
          </div>

          {/* Result Display */}
          {result && (
            <div className={`rounded-lg p-6 ${
              result.error ? 'bg-red-900/20 border border-red-500/30' : 'bg-green-900/20 border border-green-500/30'
            }`}>
              <div className="flex items-center gap-2 mb-4">
                {result.error ? (
                  <XCircle className="w-5 h-5 text-red-400" />
                ) : (
                  <CheckCircle className="w-5 h-5 text-green-400" />
                )}
                <h4 className="font-semibold text-white">
                  {result.error ? 'Processing Failed' : 'Processing Successful'}
                </h4>
              </div>
              
              {result.error ? (
                <p className="text-red-300">{result.error}</p>
              ) : (
                <div className="space-y-3 text-sm">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <span className="text-slate-400">Message Type:</span>
                      <span className="ml-2 text-white font-medium">{result.messageType}</span>
                    </div>
                    <div>
                      <span className="text-slate-400">Field Count:</span>
                      <span className="ml-2 text-white font-medium">{result.fieldCount}</span>
                    </div>
                  </div>
                  
                  <div>
                    <span className="text-slate-400">Parsed Data:</span>
                    <pre className="mt-2 p-3 bg-slate-900 rounded text-green-300 text-xs overflow-auto">
                      {JSON.stringify(result.parsedData, null, 2)}
                    </pre>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* Validate Message Tab */}
      {activeTab === 'validate' && (
        <div className="space-y-6">
          <div className="bg-slate-800/50 rounded-lg p-6 space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-white">Validate Trade Message</h3>
              <button
                onClick={loadSampleMessage}
                className="px-4 py-2 bg-slate-700 hover:bg-slate-600 text-white rounded-md transition-colors"
              >
                Load Sample
              </button>
            </div>
            
            <textarea
              value={xmlContent}
              onChange={(e) => setXmlContent(e.target.value)}
              placeholder="Paste your FIX XML trade message here for validation..."
              className="w-full h-64 p-4 bg-slate-900 border border-slate-600 rounded-md text-white placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent font-mono text-sm"
            />
            
            <button
              onClick={validateTradeMessage}
              disabled={loading}
              className="w-full py-3 bg-green-600 hover:bg-green-700 disabled:bg-slate-600 text-white rounded-md transition-colors font-medium"
            >
              {loading ? 'Validating...' : 'Validate Trade Message'}
            </button>
          </div>

          {/* Validation Result */}
          {result && (
            <div className={`rounded-lg p-6 ${
              result.error ? 'bg-red-900/20 border border-red-500/30' : 
              result.isValid ? 'bg-green-900/20 border border-green-500/30' : 
              'bg-yellow-900/20 border border-yellow-500/30'
            }`}>
              <div className="flex items-center gap-2 mb-4">
                {result.error ? (
                  <XCircle className="w-5 h-5 text-red-400" />
                ) : result.isValid ? (
                  <CheckCircle className="w-5 h-5 text-green-400" />
                ) : (
                  <XCircle className="w-5 h-5 text-yellow-400" />
                )}
                <h4 className="font-semibold text-white">
                  {result.error ? 'Validation Failed' : 
                   result.isValid ? 'Valid Trade Message' : 'Invalid Trade Message'}
                </h4>
              </div>
              
              {result.error ? (
                <p className="text-red-300">{result.error}</p>
              ) : (
                <div className="space-y-3 text-sm">
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <span className="text-slate-400">Message Type:</span>
                      <span className="ml-2 text-white font-medium">{result.messageType}</span>
                    </div>
                    <div>
                      <span className="text-slate-400">Valid:</span>
                      <span className={`ml-2 font-medium ${result.isValid ? 'text-green-400' : 'text-red-400'}`}>
                        {result.isValid ? 'YES' : 'NO'}
                      </span>
                    </div>
                  </div>
                  
                  <div>
                    <span className="text-slate-400">Details:</span>
                    <p className="mt-1 text-white">{result.validationDetails}</p>
                  </div>
                  
                  {result.errors && result.errors.length > 0 && (
                    <div>
                      <span className="text-slate-400">Errors:</span>
                      <ul className="mt-2 space-y-1">
                        {result.errors.map((error: string, index: number) => (
                          <li key={index} className="text-red-300 text-sm">• {error}</li>
                        ))}
                      </ul>
                    </div>
                  )}
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* Message History Tab */}
      {activeTab === 'history' && (
        <div className="space-y-6">
          <div className="bg-slate-800/50 rounded-lg p-6">
            <h3 className="text-lg font-semibold text-white mb-4">Recent Trade Messages</h3>
            
            {messageHistory.length === 0 ? (
              <div className="text-center py-8 text-slate-400">
                No trade messages processed yet. Process your first message to see it here.
              </div>
            ) : (
              <div className="space-y-3">
                {messageHistory.map((message) => (
                  <div key={message.id} className="flex items-center justify-between p-4 bg-slate-900/50 rounded-lg">
                    <div className="flex items-center gap-4">
                      <FileText className="w-5 h-5 text-blue-400" />
                      <div>
                        <div className="font-medium text-white">{message.type}</div>
                        <div className="text-sm text-slate-400">{message.content}</div>
                      </div>
                    </div>
                    <div className="text-right">
                      <div className={`px-2 py-1 rounded text-xs font-medium ${
                        message.status === 'PROCESSED' ? 'bg-green-900/50 text-green-300' : 'bg-red-900/50 text-red-300'
                      }`}>
                        {message.status}
                      </div>
                      <div className="text-xs text-slate-400 mt-1">
                        {new Date(message.timestamp).toLocaleString()}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
