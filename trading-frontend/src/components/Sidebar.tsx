'use client'

import { useState } from 'react'
import { 
  LayoutDashboard, 
  TrendingUp, 
  Briefcase, 
  Code, 
  ArrowLeftRight, 
  Users,
  ChevronLeft,
  ChevronRight,
  LogOut
} from 'lucide-react'

interface SidebarProps {
  activeSection: string
  onSectionChange: (section: string) => void
  onLogout?: () => void
}

export default function Sidebar({ activeSection, onSectionChange, onLogout }: SidebarProps) {
  const [isCollapsed, setIsCollapsed] = useState(false)

  const menuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { id: 'trading', label: 'Trading', icon: TrendingUp },
    { id: 'portfolio', label: 'Portfolio', icon: Briefcase },
    { id: 'xml-processing', label: 'XML Processing', icon: Code },
    { id: 'trade-messages', label: 'Trade Messages', icon: ArrowLeftRight },
    { id: 'ioi', label: 'IOI Management', icon: Users },
  ]

  return (
    <div className="w-64 h-screen p-4 flex flex-col">
      {/* Header */}
      <div className="p-4 border-b border-slate-700">
        <div className="flex items-center justify-between">
          {!isCollapsed && (
            <div className="flex items-center gap-2">
              <TrendingUp className="w-6 h-6 text-blue-400" />
              <h2 className="text-blue-400 font-semibold">Trading Platform</h2>
            </div>
          )}
          <button
            onClick={() => setIsCollapsed(!isCollapsed)}
            className="p-1 rounded hover:bg-slate-800 text-slate-400 hover:text-white"
          >
            {isCollapsed ? <ChevronRight className="w-4 h-4" /> : <ChevronLeft className="w-4 h-4" />}
          </button>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 p-4">
        <div className="space-y-2">
          {menuItems.map((item) => {
            const Icon = item.icon
            const isActive = activeSection === item.id
            
            return (
              <button
                key={item.id}
                onClick={() => onSectionChange(item.id)}
                className={`flex items-center space-x-3 p-3 rounded-lg transition-colors ${
                  isActive
                    ? 'text-blue-400'
                    : 'text-slate-300 hover:text-blue-300'
                }`}
                title={isCollapsed ? item.label : undefined}
              >
                <Icon className="w-5 h-5 flex-shrink-0" />
                {!isCollapsed && <span className="font-medium">{item.label}</span>}
              </button>
            )
          })}
        </div>
      </nav>

      {/* Logout Button */}
      {onLogout && (
        <div className="p-4 border-t border-slate-700">
          <button
            onClick={onLogout}
            className="flex items-center space-x-3 p-3 rounded-lg transition-colors text-red-400 hover:text-red-300 hover:bg-red-900/20 w-full"
            title={isCollapsed ? 'Logout' : undefined}
          >
            <LogOut className="w-5 h-5 flex-shrink-0" />
            {!isCollapsed && <span className="font-medium">Logout</span>}
          </button>
        </div>
      )}
    </div>
  )
}
