import { request } from './http'

export function listStocks() {
  return request('/api/stock/list')
}

export function listWarehouses() {
  return request('/api/stock/warehouse/list')
}
