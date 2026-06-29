import { existsSync, readFileSync } from 'node:fs'
import { basename, dirname, resolve } from 'node:path'

const root = resolve(import.meta.dirname, '..')
let repoRoot = root
while (!existsSync(resolve(repoRoot, '.git'))) {
  const parent = dirname(repoRoot)
  if (parent === repoRoot) {
    throw new Error('Cannot locate repository root from frontend smoke check')
  }
  repoRoot = parent
}

if (basename(root) !== 'frontend' || dirname(root) !== repoRoot) {
  throw new Error('Frontend project must be placed at repository root: /frontend')
}

if (existsSync(resolve(repoRoot, 'distribution-platform', 'frontend'))) {
  throw new Error('Frontend project must not be nested inside distribution-platform')
}

const requiredFiles = [
  'package.json',
  'index.html',
  'vite.config.js',
  'src/main.js',
  'src/App.vue',
  'src/router/index.js',
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
  'src/api/product.js',
  'src/api/stock.js',
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

for (const dependency of ['vue-router', 'pinia']) {
  if (!packageJson.dependencies?.[dependency]) {
    throw new Error(`Missing frontend dependency: ${dependency}`)
  }
}

const mainJs = readFileSync(resolve(root, 'src/main.js'), 'utf8')
for (const requiredSnippet of ['ElementPlus', 'router', 'createPinia']) {
  if (!mainJs.includes(requiredSnippet)) {
    throw new Error(`main.js must register ${requiredSnippet}`)
  }
}

const appVue = readFileSync(resolve(root, 'src/App.vue'), 'utf8')
if (!appVue.includes('RouterView')) {
  throw new Error('App.vue must render pages through Vue Router')
}

const routerIndex = readFileSync(resolve(root, 'src/router/index.js'), 'utf8')
for (const routeName of ['governance', 'orders', 'dashboard', 'ai', 'monitor']) {
  if (!routerIndex.includes(routeName)) {
    throw new Error(`router/index.js does not register route: ${routeName}`)
  }
}

const orderView = readFileSync(resolve(root, 'src/views/OrderFulfillment.vue'), 'utf8')
for (const label of ['订单号', '商品', '数量', '金额', '状态', '库存服务', '支付服务', '时间']) {
  if (!orderView.includes(label)) {
    throw new Error(`Order page missing table label: ${label}`)
  }
}

const dashboardView = readFileSync(resolve(root, 'src/views/DataDashboard.vue'), 'utf8')
for (const label of ['商品管理（CRUD）', '库存查询', '商品编码', '库存服务']) {
  if (!dashboardView.includes(label)) {
    throw new Error(`Dashboard page missing frontend联调 label: ${label}`)
  }
}

const viteConfig = readFileSync(resolve(root, 'vite.config.js'), 'utf8')
if (!viteConfig.includes('localhost:9000')) {
  throw new Error('vite.config.js must proxy API requests to localhost:9000')
}

const httpApi = readFileSync(resolve(root, 'src/api/http.js'), 'utf8')
if (!httpApi.includes('localhost:9000')) {
  throw new Error('http.js must point Axios baseURL to localhost:9000')
}

const orderApi = readFileSync(resolve(root, 'src/api/order.js'), 'utf8')
if (!orderApi.includes('/api/order/dashboard')) {
  throw new Error('order.js must request /api/order/dashboard')
}

const productApi = readFileSync(resolve(root, 'src/api/product.js'), 'utf8')
if (!productApi.includes('/api/product/list')) {
  throw new Error('product.js must request /api/product/list')
}

const stockApi = readFileSync(resolve(root, 'src/api/stock.js'), 'utf8')
for (const path of ['/api/stock/list', '/api/stock/warehouse/list']) {
  if (!stockApi.includes(path)) {
    throw new Error(`stock.js must request ${path}`)
  }
}

console.log('frontend scaffold smoke check passed')
