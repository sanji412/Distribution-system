import { request } from './http'

export function getOrderDashboard() {
  return request('/api/order/dashboard')
}
