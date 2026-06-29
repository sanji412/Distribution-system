import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:9000',
  timeout: 5000
})

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
