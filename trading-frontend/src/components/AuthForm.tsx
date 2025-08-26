'use client'

import { useState } from 'react'
import axios from 'axios'
import { BackgroundCircles } from './ui/background-circles'

interface AuthFormProps {
  onAuthSuccess: (userData: any, token: string) => void
}

export function AuthForm({ onAuthSuccess }: AuthFormProps) {
  const [isLogin, setIsLogin] = useState(true)
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    email: '',
    fullName: ''
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError('')

    try {
      const endpoint = isLogin ? '/api/auth/signin' : '/api/auth/signup'
      const requestData = {
        username: formData.username,
        password: formData.password,
        ...(isLogin ? {} : { email: formData.email, fullName: formData.fullName })
      }
      
      console.log('Making request to:', `http://localhost:8080${endpoint}`)
      console.log('Request data:', requestData)
      
      const response = await axios.post(`http://localhost:8080${endpoint}`, requestData, {
        headers: {
          'Content-Type': 'application/json'
        }
      })

      console.log('Response:', response.data)

      if (response.data.success) {
        onAuthSuccess(response.data.user, response.data.token)
      } else {
        setError(response.data.message || 'Authentication failed')
      }
    } catch (err: any) {
      console.error('Auth error:', err)
      console.error('Error response:', err.response)
      setError(err.response?.data?.message || err.message || 'Network error')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen relative">
      <BackgroundCircles title="" description="" variant="primary" />
      <div className="absolute inset-0 flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
        <div className="w-full max-w-md p-8 space-y-6 bg-slate-800/80 backdrop-blur-sm rounded-lg shadow-lg border border-slate-700 relative z-20">
        <h2 className="text-2xl font-bold text-center text-slate-100">
          {isLogin ? 'Sign In' : 'Sign Up'}
        </h2>
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div>
            <label className="block text-sm font-medium text-slate-300">
              Username
            </label>
            <input
              type="text"
              value={formData.username}
              onChange={(e) => setFormData({ ...formData, username: e.target.value })}
              className="w-full px-3 py-2 mt-1 border border-slate-600 rounded-md bg-slate-700/50 text-slate-100 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>
          {!isLogin && (
            <>
              <div>
                <label className="block text-sm font-medium text-slate-300">
                  Full Name
                </label>
                <input
                  type="text"
                  value={formData.fullName}
                  onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                  className="w-full px-3 py-2 mt-1 border border-slate-600 rounded-md bg-slate-700/50 text-slate-100 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-300">
                  Email address
                </label>
                <input
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  className="w-full px-3 py-2 mt-1 border border-slate-600 rounded-md bg-slate-700/50 text-slate-100 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>
            </>
          )}
          <div>
            <label className="block text-sm font-medium text-slate-300">
              Password
            </label>
            <input
              type="password"
              value={formData.password}
              onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              className="w-full px-3 py-2 mt-1 border border-slate-600 rounded-md bg-slate-700/50 text-slate-100 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          {error && (
            <div className="p-3 text-sm text-red-300 bg-red-900/50 border border-red-700/50 rounded-md">
              {error}
            </div>
          )}

          <div>
            <button
              type="submit"
              disabled={loading}
              className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50"
            >
              {loading ? 'Loading...' : (isLogin ? 'Sign in' : 'Sign up')}
            </button>
          </div>

          <div className="text-center">
            <button
              type="button"
              onClick={() => setIsLogin(!isLogin)}
              className="text-sm text-blue-400 hover:text-blue-300 underline"
            >
              {isLogin ? "Don't have an account? Sign up" : "Already have an account? Sign in"}
            </button>
          </div>

          <div className="text-center">
            <p className="text-sm text-gray-600">
              Test credentials: <strong>testuser</strong> / <strong>password</strong>
            </p>
          </div>
        </form>
        </div>
      </div>
    </div>
  )
}
