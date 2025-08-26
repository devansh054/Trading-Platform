import type { Metadata } from 'next'
import { Inter } from 'next/font/google'
import './globals.css'
import { BackgroundCircles } from '@/components/ui/background-circles'

const inter = Inter({ subsets: ['latin'] })

export const metadata: Metadata = {
  title: 'Trading Dashboard',
  description: 'Professional trading platform with real-time data',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className={inter.className} suppressHydrationWarning style={{position: 'relative', minHeight: '100vh'}}>
        <div style={{position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh', zIndex: 0}}>
          <BackgroundCircles 
            title="" 
            description=""
            variant="senary"
            className="h-full w-full"
          />
        </div>
        <div style={{position: 'relative', zIndex: 10}}>
          {children}
        </div>
      </body>
    </html>
  )
}
