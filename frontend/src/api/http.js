import axios from 'axios'
import { clearUserAuth, getUserAuth } from '../auth/userAuth'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://127.0.0.1:8081/api',
  timeout: 10000,
})

api.interceptors.request.use((config) => {
  try {
    const isAdminRequest = String(config.url || '').startsWith('/admin/')
    const raw = isAdminRequest ? localStorage.getItem('musicdemo1.admin.auth') : null
    const auth = raw ? JSON.parse(raw) : getUserAuth()
    if (auth?.token) config.headers.Authorization = `Bearer ${auth.token}`
  } catch {
    // Private browsing or malformed local state is treated as logged out.
  }
  return config
})

api.interceptors.response.use((response) => response, (error) => {
  if (error.response?.status === 401) {
    const requestUrl = String(error.config?.url || '')
    if (requestUrl.startsWith('/admin/') && requestUrl !== '/admin/auth/login') {
      try { localStorage.removeItem('musicdemo1.admin.auth') } catch { /* ignore storage failures */ }
    }
    if (requestUrl.startsWith('/user/')) {
      clearUserAuth()
      window.dispatchEvent(new CustomEvent('musicdemo1:user-session-expired'))
    }
  }
  return Promise.reject(error)
})
