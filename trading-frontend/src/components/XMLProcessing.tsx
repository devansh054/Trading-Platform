'use client'

import { useState } from 'react'
import { Code, Search, CheckCircle, ArrowLeftRight, Layers, FileCode, Trash2 } from 'lucide-react'

export default function XMLProcessing() {
  const [activeTab, setActiveTab] = useState('parser')
  const [xmlInput, setXmlInput] = useState('')
  const [xmlValidateInput, setXmlValidateInput] = useState('')
  const [xmlTransformInput, setXmlTransformInput] = useState('')
  const [batchXmlInput, setBatchXmlInput] = useState('')
  const [messageType, setMessageType] = useState('NewOrderSingle')
  const [results, setResults] = useState({
    parse: '',
    validate: '',
    transform: '',
    batch: ''
  })
  const [loading, setLoading] = useState({
    parse: false,
    validate: false,
    transform: false,
    batch: false
  })

  const sampleXML = `<?xml version="1.0" encoding="UTF-8"?>
<TradeMessage>
    <Header>
        <MsgType>D</MsgType>
        <SenderCompID>TRADER001</SenderCompID>
        <TargetCompID>EXCHANGE</TargetCompID>
        <SendingTime>20231201-10:30:00</SendingTime>
    </Header>
    <Body>
        <NewOrderSingle>
            <ClOrdID>ORDER123</ClOrdID>
            <Symbol>AAPL</Symbol>
            <Side>1</Side>
            <OrderQty>100</OrderQty>
            <OrdType>2</OrdType>
            <Price>150.50</Price>
            <TimeInForce>0</TimeInForce>
        </NewOrderSingle>
    </Body>
</TradeMessage>`

  const parseXMLMessage = async () => {
    setLoading(prev => ({ ...prev, parse: true }))
    try {
      const response = await fetch('http://localhost:8080/api/xml/parse', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        },
        body: JSON.stringify({ xmlContent: xmlInput })
      })

      const data = await response.json()
      
      if (response.ok) {
        setResults(prev => ({ 
          ...prev, 
          parse: `✅ XML Parsed Successfully!\n\nParsed Data:\n${JSON.stringify(data.parsedData, null, 2)}\n\nMessage Type: ${data.messageType}\nFields Found: ${data.fieldCount}`
        }))
      } else {
        setResults(prev => ({ 
          ...prev, 
          parse: `❌ Parse Error: ${data.message || 'Failed to parse XML'}`
        }))
      }
    } catch (error) {
      setResults(prev => ({ 
        ...prev, 
        parse: `❌ Network Error: ${error}`
      }))
    }
    setLoading(prev => ({ ...prev, parse: false }))
  }

  const validateXML = async () => {
    setLoading(prev => ({ ...prev, validate: true }))
    try {
      const response = await fetch('http://localhost:8080/api/xml/validate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        },
        body: JSON.stringify({ 
          xmlContent: xmlValidateInput,
          messageType: messageType
        })
      })

      const data = await response.json()
      
      if (response.ok) {
        setResults(prev => ({ 
          ...prev, 
          validate: `✅ XML Validation Result:\n\nValid: ${data.isValid ? 'YES' : 'NO'}\nMessage Type: ${data.messageType}\n\nValidation Details:\n${data.validationDetails || 'No additional details'}\n\nErrors: ${data.errors?.length || 0}`
        }))
      } else {
        setResults(prev => ({ 
          ...prev, 
          validate: `❌ Validation Error: ${data.message || 'Failed to validate XML'}`
        }))
      }
    } catch (error) {
      setResults(prev => ({ 
        ...prev, 
        validate: `❌ Network Error: ${error}`
      }))
    }
    setLoading(prev => ({ ...prev, validate: false }))
  }

  const transformXMLToJSON = async () => {
    setLoading(prev => ({ ...prev, transform: true }))
    try {
      const response = await fetch('http://localhost:8080/api/xml/transform', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        },
        body: JSON.stringify({ xmlContent: xmlTransformInput })
      })

      const data = await response.json()
      
      if (response.ok) {
        setResults(prev => ({ 
          ...prev, 
          transform: `✅ XML to JSON Transformation Complete!\n\nJSON Output:\n${JSON.stringify(data.jsonData, null, 2)}`
        }))
      } else {
        setResults(prev => ({ 
          ...prev, 
          transform: `❌ Transform Error: ${data.message || 'Failed to transform XML'}`
        }))
      }
    } catch (error) {
      setResults(prev => ({ 
        ...prev, 
        transform: `❌ Network Error: ${error}`
      }))
    }
    setLoading(prev => ({ ...prev, transform: false }))
  }

  const processBatchXML = async () => {
    setLoading(prev => ({ ...prev, batch: true }))
    try {
      const response = await fetch('http://localhost:8080/api/xml/batch', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        },
        body: JSON.stringify({ xmlBatch: batchXmlInput })
      })

      const data = await response.json()
      
      if (response.ok) {
        setResults(prev => ({ 
          ...prev, 
          batch: `✅ Batch Processing Complete!\n\nProcessed: ${data.processedCount} messages\nSuccessful: ${data.successCount}\nErrors: ${data.errorCount}\n\nResults:\n${JSON.stringify(data.results, null, 2)}`
        }))
      } else {
        setResults(prev => ({ 
          ...prev, 
          batch: `❌ Batch Error: ${data.message || 'Failed to process batch'}`
        }))
      }
    } catch (error) {
      setResults(prev => ({ 
        ...prev, 
        batch: `❌ Network Error: ${error}`
      }))
    }
    setLoading(prev => ({ ...prev, batch: false }))
  }

  const loadSampleXML = () => {
    setXmlInput(sampleXML)
  }

  const loadBatchSample = () => {
    setBatchXmlInput(`${sampleXML}\n\n${sampleXML.replace('ORDER123', 'ORDER124').replace('AAPL', 'MSFT')}`)
  }

  const tabs = [
    { id: 'parser', label: 'XML Parser', icon: Search },
    { id: 'validator', label: 'XML Validator', icon: CheckCircle },
    { id: 'transformer', label: 'XML Transformer', icon: ArrowLeftRight },
    { id: 'batch', label: 'Batch Processor', icon: Layers }
  ]

  return (
    <div className="space-y-6">
      <div className="bg-slate-800/30 backdrop-blur-sm rounded-lg border border-slate-700/50 shadow-xl">
        <div className="p-6 border-b border-slate-700/50">
          <h2 className="text-2xl font-bold bg-gradient-to-r from-blue-400 to-purple-400 bg-clip-text text-transparent flex items-center gap-2">
            <Code className="w-6 h-6 text-blue-400" />
            XML Processing - Advanced Trade Message Parsing
          </h2>
          <p className="text-slate-300 mt-2">
            Parse, validate, transform and batch process XML trade messages
          </p>
        </div>
        
        <div className="p-6">
          {/* Custom Tab Navigation */}
          <div className="flex space-x-1 rounded-lg bg-slate-900/50 p-1 mb-6">
            {tabs.map((tab) => {
              const Icon = tab.icon
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex-1 rounded-md px-3 py-2 text-sm font-medium transition-colors flex items-center justify-center gap-2 ${
                    activeTab === tab.id
                      ? 'bg-blue-600 text-white'
                      : 'text-slate-300 hover:text-white hover:bg-slate-700/50'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  {tab.label}
                </button>
              )
            })}
          </div>

          {/* Parser Tab */}
          {activeTab === 'parser' && (
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-200 mb-2">XML Trade Message</label>
                <textarea
                  value={xmlInput}
                  onChange={(e) => setXmlInput(e.target.value)}
                  placeholder="Paste XML trade message here for parsing..."
                  className="w-full min-h-[300px] bg-slate-900/50 border border-slate-600 rounded-lg px-3 py-2 text-slate-200 font-mono text-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              <div className="flex gap-2">
                <button 
                  onClick={parseXMLMessage} 
                  disabled={loading.parse}
                  className="bg-blue-600 hover:bg-blue-700 disabled:bg-slate-600 text-white px-4 py-2 rounded-lg transition-colors flex items-center gap-2"
                >
                  <Search className="w-4 h-4" />
                  {loading.parse ? 'Parsing...' : 'Parse XML'}
                </button>
                <button 
                  onClick={loadSampleXML}
                  className="bg-slate-700 hover:bg-slate-600 text-slate-200 px-4 py-2 rounded-lg transition-colors flex items-center gap-2"
                >
                  <FileCode className="w-4 h-4" />
                  Load Sample XML
                </button>
                <button 
                  onClick={() => setXmlInput('')}
                  className="bg-slate-700 hover:bg-slate-600 text-slate-200 px-4 py-2 rounded-lg transition-colors flex items-center gap-2"
                >
                  <Trash2 className="w-4 h-4" />
                  Clear
                </button>
              </div>
              {results.parse && (
                <div className={`p-4 rounded-lg font-mono text-sm whitespace-pre-line border ${
                  results.parse.includes('✅') ? 'bg-green-900/20 border-green-500/50 text-green-300' :
                  'bg-red-900/20 border-red-500/50 text-red-300'
                }`}>
                  {results.parse}
                </div>
              )}
            </div>
          )}

          {/* Validator Tab */}
          {activeTab === 'validator' && (
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-200 mb-2">XML Message to Validate</label>
                <textarea
                  value={xmlValidateInput}
                  onChange={(e) => setXmlValidateInput(e.target.value)}
                  placeholder="Paste XML for validation..."
                  className="w-full min-h-[300px] bg-slate-900/50 border border-slate-600 rounded-lg px-3 py-2 text-slate-200 font-mono text-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-200 mb-2">Message Type</label>
                <select 
                  value={messageType} 
                  onChange={(e) => setMessageType(e.target.value)}
                  className="w-full bg-slate-900/50 border border-slate-600 rounded-lg px-3 py-2 text-slate-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                >
                  <option value="NewOrderSingle">New Order Single</option>
                  <option value="OrderCancelRequest">Order Cancel Request</option>
                  <option value="MarketDataRequest">Market Data Request</option>
                  <option value="TradeReport">Trade Report</option>
                  <option value="ExecutionReport">Execution Report</option>
                </select>
              </div>
              <button 
                onClick={validateXML} 
                disabled={loading.validate}
                className="bg-blue-600 hover:bg-blue-700 disabled:bg-slate-600 text-white px-4 py-2 rounded-lg transition-colors flex items-center gap-2"
              >
                <CheckCircle className="w-4 h-4" />
                {loading.validate ? 'Validating...' : 'Validate XML'}
              </button>
              {results.validate && (
                <div className={`p-4 rounded-lg font-mono text-sm whitespace-pre-line border ${
                  results.validate.includes('✅') ? 'bg-green-900/20 border-green-500/50 text-green-300' :
                  'bg-red-900/20 border-red-500/50 text-red-300'
                }`}>
                  {results.validate}
                </div>
              )}
            </div>
          )}

          {/* Transformer Tab */}
          {activeTab === 'transformer' && (
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-200 mb-2">XML Input</label>
                <textarea
                  value={xmlTransformInput}
                  onChange={(e) => setXmlTransformInput(e.target.value)}
                  placeholder="XML to transform..."
                  className="w-full min-h-[300px] bg-slate-900/50 border border-slate-600 rounded-lg px-3 py-2 text-slate-200 font-mono text-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              <button 
                onClick={transformXMLToJSON} 
                disabled={loading.transform}
                className="bg-blue-600 hover:bg-blue-700 disabled:bg-slate-600 text-white px-4 py-2 rounded-lg transition-colors flex items-center gap-2"
              >
                <ArrowLeftRight className="w-4 h-4" />
                {loading.transform ? 'Transforming...' : 'Transform to JSON'}
              </button>
              {results.transform && (
                <div className={`p-4 rounded-lg font-mono text-sm whitespace-pre-line border ${
                  results.transform.includes('✅') ? 'bg-green-900/20 border-green-500/50 text-green-300' :
                  'bg-red-900/20 border-red-500/50 text-red-300'
                }`}>
                  {results.transform}
                </div>
              )}
            </div>
          )}

          {/* Batch Tab */}
          {activeTab === 'batch' && (
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-200 mb-2">Multiple XML Messages (separated by empty lines)</label>
                <textarea
                  value={batchXmlInput}
                  onChange={(e) => setBatchXmlInput(e.target.value)}
                  placeholder="Paste multiple XML messages here..."
                  className="w-full min-h-[300px] bg-slate-900/50 border border-slate-600 rounded-lg px-3 py-2 text-slate-200 font-mono text-sm placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
              <div className="flex gap-2">
                <button 
                  onClick={processBatchXML} 
                  disabled={loading.batch}
                  className="bg-blue-600 hover:bg-blue-700 disabled:bg-slate-600 text-white px-4 py-2 rounded-lg transition-colors flex items-center gap-2"
                >
                  <Layers className="w-4 h-4" />
                  {loading.batch ? 'Processing...' : 'Process Batch'}
                </button>
                <button 
                  onClick={loadBatchSample}
                  className="bg-slate-700 hover:bg-slate-600 text-slate-200 px-4 py-2 rounded-lg transition-colors flex items-center gap-2"
                >
                  <FileCode className="w-4 h-4" />
                  Load Batch Sample
                </button>
              </div>
              {results.batch && (
                <div className={`p-4 rounded-lg font-mono text-sm whitespace-pre-line border ${
                  results.batch.includes('✅') ? 'bg-green-900/20 border-green-500/50 text-green-300' :
                  'bg-red-900/20 border-red-500/50 text-red-300'
                }`}>
                  {results.batch}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
