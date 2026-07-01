import { request } from './http'

export function getOrderDashboard() {
  return request('/api/order/dashboard')
}

export function getOrderAnalysis() {
  return request('/api/order/analysis')
}
