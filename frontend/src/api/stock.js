import { request } from './http'

export function listStocks() {
  return request('/api/stock/list')
}

export function listWarehouses() {
  return request('/api/stock/warehouse/list')
}

export function listSentinelRules() {
  return request('/api/stock/sentinel/rules')
}
