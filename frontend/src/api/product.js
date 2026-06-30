import { request } from './http'

export function listProducts() {
  return request('/api/product/list')
}

export function createProduct(product) {
  return request('/api/product/create', {
    method: 'POST',
    data: product
  })
}

export function updateProduct(productId, product) {
  return request(`/api/product/${productId}`, {
    method: 'PUT',
    data: product
  })
}

export function deleteProduct(productId) {
  return request(`/api/product/${productId}`, {
    method: 'DELETE'
  })
}
