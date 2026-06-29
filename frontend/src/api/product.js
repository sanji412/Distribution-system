import { request } from './http'

export function listProducts() {
  return request('/api/product/list')
}
