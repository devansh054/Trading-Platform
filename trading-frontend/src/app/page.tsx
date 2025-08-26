'use client'

import { useState, useEffect } from 'react'
import { AuthForm } from '@/components/AuthForm'
import Sidebar from '@/components/Sidebar'
import Dashboard from '@/components/Dashboard'
import Trading from '@/components/Trading'
import Portfolio from '@/components/Portfolio'
import IOIManagement from '@/components/IOIManagement'
import XMLProcessing from '@/components/XMLProcessing'
import TradeMessages from '@/components/TradeMessages'

export default function Home() {
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [loading, setLoading] = useState(true)
  const [activeSection, setActiveSection] = useState('dashboard')

  useEffect(() => {
    const token = localStorage.getItem('authToken')
    const user = localStorage.getItem('currentUser')
    
    if (token && user) {
      setIsAuthenticated(true)
    }
    setLoading(false)
  }, [])

  const handleAuthSuccess = (userData: any, token: string) => {
    localStorage.setItem('authToken', token)
    localStorage.setItem('currentUser', JSON.stringify(userData))
    setIsAuthenticated(true)
  }

  const handleLogout = () => {
    localStorage.removeItem('authToken')
    localStorage.removeItem('currentUser')
    setIsAuthenticated(false)
  }

  const renderActiveSection = () => {
    switch (activeSection) {
      case 'dashboard':
        return <Dashboard onLogout={handleLogout} />
      case 'portfolio':
        return <Portfolio onLogout={handleLogout} />
      case 'trading':
        return <Trading onLogout={handleLogout} />
      case 'xml-processing':
        return <XMLProcessing />
      case 'trade-messages':
        return <TradeMessages />
      case 'ioi':
        return <IOIManagement />
      default:
        return <Dashboard onLogout={handleLogout} />
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="text-white">Loading...</div>
      </div>
    )
  }

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen">
        <AuthForm onAuthSuccess={handleAuthSuccess} />
      </div>
    )
  }

  return (
    <div className="min-h-screen flex">
      <Sidebar activeSection={activeSection} onSectionChange={setActiveSection} onLogout={handleLogout} />
      <main className="flex-1">
        {renderActiveSection()}
      </main>
    </div>
  )
}
