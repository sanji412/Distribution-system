import { request } from './http'

export function sendAiMessage(payload) {
  return request('/api/order/ai/chat', {
    method: 'POST',
    data: payload
  })
}

export function getAiPromptTemplate() {
  return request('/api/order/ai/prompt-template')
}
