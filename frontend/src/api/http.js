import axios from 'axios'
import { clearAuthSession, getToken } from './session'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 30000
})

http.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearAuthSession()
      if (window.location.hash !== '#/login') {
        window.location.hash = '#/login'
      }
    }
    return Promise.reject(error)
  }
)

export async function request(path, options = {}) {
  const response = await http.request({
    url: path,
    ...options
  })

  const result = response.data
  if (result && typeof result === 'object' && 'code' in result) {
    if (result.code !== 200) {
      throw new Error(result.msg || '接口请求失败')
    }
    return result.data
  }

  return result
}
