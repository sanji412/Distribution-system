import { request } from './http'

export function login(payload) {
  return request('/api/auth/login', {
    method: 'POST',
    data: payload
  })
}

export function validateToken() {
  return request('/api/auth/validate')
}
