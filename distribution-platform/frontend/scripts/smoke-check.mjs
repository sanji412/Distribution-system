import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const root = resolve(import.meta.dirname, '..')
const requiredFiles = [
  'package.json',
  'index.html',
  'vite.config.js',
  'src/main.js',
  'src/App.vue',
  'src/components/AppHeader.vue',
  'src/components/SideRail.vue',
  'src/components/PanelBox.vue',
  'src/components/MetricCard.vue',
  'src/components/StatusTag.vue',
  'src/views/ServiceGovernance.vue',
  'src/views/OrderFulfillment.vue',
  'src/views/DataDashboard.vue',
  'src/views/AiCustomerService.vue',
  'src/views/SystemMonitor.vue',
  'src/api/http.js',
  'src/api/order.js',
  'src/api/mock.js'
]

for (const file of requiredFiles) {
  const fullPath = resolve(root, file)
  if (!existsSync(fullPath)) {
    throw new Error(`Missing required frontend file: ${file}`)
  }
}

const packageJson = JSON.parse(readFileSync(resolve(root, 'package.json'), 'utf8'))
for (const script of ['dev', 'build', 'test:smoke']) {
  if (!packageJson.scripts?.[script]) {
    throw new Error(`Missing package script: ${script}`)
  }
}

const appVue = readFileSync(resolve(root, 'src/App.vue'), 'utf8')
for (const pageId of ['governance', 'orders', 'dashboard', 'ai', 'monitor']) {
  if (!appVue.includes(pageId)) {
    throw new Error(`App.vue does not register page id: ${pageId}`)
  }
}

const orderView = readFileSync(resolve(root, 'src/views/OrderFulfillment.vue'), 'utf8')
for (const label of ['订单号', '商品', '数量', '金额', '状态', '库存服务', '支付服务', '时间']) {
  if (!orderView.includes(label)) {
    throw new Error(`Order page missing table label: ${label}`)
  }
}

const viteConfig = readFileSync(resolve(root, 'vite.config.js'), 'utf8')
if (!viteConfig.includes('localhost:8082')) {
  throw new Error('vite.config.js must proxy order API to localhost:8082')
}

const orderApi = readFileSync(resolve(root, 'src/api/order.js'), 'utf8')
if (!orderApi.includes('/api/order/dashboard')) {
  throw new Error('order.js must request /api/order/dashboard')
}

console.log('frontend scaffold smoke check passed')
