import { request } from './http'

export function getOrderDashboard() {
  return request('/api/order/dashboard')
}

export function getOrderAnalysis() {
  return request('/api/order/analysis')
}

export function getSeataFlow() {
  return request('/api/order/seata/flow')
}
