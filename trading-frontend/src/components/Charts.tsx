'use client'

import { useEffect, useRef, useMemo } from 'react'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  BarElement,
} from 'chart.js'
import { Line, Bar } from 'react-chartjs-2'

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  BarElement
)

interface PortfolioChartProps {
  data?: any[]
}

export function PortfolioChart({ data = [] }: PortfolioChartProps) {
  const chartRef = useRef<any>(null)
  
  // Use useMemo to recalculate chart data when portfolio data changes
  const chartData = useMemo(() => {
    const colors = [
      'rgba(16, 185, 129, 0.8)',
      'rgba(59, 130, 246, 0.8)',
      'rgba(245, 158, 11, 0.8)',
      'rgba(239, 68, 68, 0.8)',
      'rgba(139, 92, 246, 0.8)',
      'rgba(34, 197, 94, 0.8)',
      'rgba(168, 85, 247, 0.8)',
    ]
    
    const borderColors = [
      'rgb(16, 185, 129)',
      'rgb(59, 130, 246)',
      'rgb(245, 158, 11)',
      'rgb(239, 68, 68)',
      'rgb(139, 92, 246)',
      'rgb(34, 197, 94)',
      'rgb(168, 85, 247)',
    ]
    
    return {
      labels: data.length > 0 ? data.map(holding => holding.symbol) : ['No Holdings'],
      datasets: [
        {
          label: 'Market Value ($)',
          data: data.length > 0 ? data.map(holding => holding.currentValue) : [0],
          backgroundColor: data.map((_, index) => colors[index % colors.length]),
          borderColor: data.map((_, index) => borderColors[index % borderColors.length]),
          borderWidth: 2,
        },
      ],
    }
  }, [data])
  
  // Force chart update when data changes
  useEffect(() => {
    if (chartRef.current) {
      chartRef.current.update('none') // Update without animation for real-time feel
    }
  }, [data])

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top' as const,
        labels: {
          color: '#e2e8f0',
        },
      },
      title: {
        display: true,
        text: 'Portfolio Performance',
        color: '#e2e8f0',
      },
    },
    scales: {
      x: {
        ticks: {
          color: '#94a3b8',
        },
        grid: {
          color: '#334155',
        },
      },
      y: {
        ticks: {
          color: '#94a3b8',
        },
        grid: {
          color: '#334155',
        },
      },
    },
  }

  return (
    <div className="h-96">
      {data.length > 0 ? (
        <Bar ref={chartRef} data={chartData} options={options} />
      ) : (
        <div className="flex items-center justify-center h-full text-slate-300">
          No portfolio data to display
        </div>
      )}
    </div>
  )
}

interface MarketChartProps {
  data?: {[key: string]: {price: number, change: number}}
}

export function MarketChart({ data = {} }: MarketChartProps) {
  const chartRef = useRef<any>(null)
  const symbols = Object.keys(data)
  
  // Use useMemo to recalculate chart data when market data changes
  const chartData = useMemo(() => {
    const colors = [
      'rgba(16, 185, 129, 0.8)',
      'rgba(59, 130, 246, 0.8)',
      'rgba(245, 158, 11, 0.8)',
      'rgba(239, 68, 68, 0.8)',
      'rgba(139, 92, 246, 0.8)',
      'rgba(34, 197, 94, 0.8)',
      'rgba(168, 85, 247, 0.8)',
    ]
    
    const borderColors = [
      'rgb(16, 185, 129)',
      'rgb(59, 130, 246)',
      'rgb(245, 158, 11)',
      'rgb(239, 68, 68)',
      'rgb(139, 92, 246)',
      'rgb(34, 197, 94)',
      'rgb(168, 85, 247)',
    ]
    
    return {
      labels: symbols.length > 0 ? symbols : ['No Data'],
      datasets: [
        {
          label: 'Price ($)',
          data: symbols.length > 0 ? symbols.map(symbol => data[symbol].price) : [0],
          backgroundColor: symbols.map((_, index) => colors[index % colors.length]),
          borderColor: symbols.map((_, index) => borderColors[index % borderColors.length]),
          borderWidth: 2,
        },
      ],
    }
  }, [data, symbols])
  
  // Force chart update when data changes
  useEffect(() => {
    if (chartRef.current) {
      chartRef.current.update('none') // Update without animation for real-time feel
    }
  }, [data])

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: false,
      },
      title: {
        display: true,
        text: 'Market Prices',
        color: '#e2e8f0',
      },
    },
    scales: {
      x: {
        ticks: {
          color: '#94a3b8',
        },
        grid: {
          color: '#334155',
        },
      },
      y: {
        ticks: {
          color: '#94a3b8',
        },
        grid: {
          color: '#334155',
        },
      },
    },
  }

  return (
    <div className="h-96">
      {symbols.length > 0 ? (
        <Bar ref={chartRef} data={chartData} options={options} />
      ) : (
        <div className="flex items-center justify-center h-full text-slate-300">
          No market data to display
        </div>
      )}
    </div>
  )
}
