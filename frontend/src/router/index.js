import { createRouter, createWebHashHistory } from 'vue-router'
import ServiceGovernance from '../views/ServiceGovernance.vue'
import OrderFulfillment from '../views/OrderFulfillment.vue'
import DataDashboard from '../views/DataDashboard.vue'
import AiCustomerService from '../views/AiCustomerService.vue'
import SystemMonitor from '../views/SystemMonitor.vue'
import LoginView from '../views/LoginView.vue'
import { isAuthenticated } from '../api/session'

const routes = [
  {
    path: '/',
    redirect: '/governance'
  },
  {
    path: '/login',
    name: 'login',
    component: LoginView,
    meta: { public: true }
  },
  {
    path: '/governance',
    name: 'governance',
    component: ServiceGovernance
  },
  {
    path: '/orders',
    name: 'orders',
    component: OrderFulfillment
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: DataDashboard
  },
  {
    path: '/ai',
    name: 'ai',
    component: AiCustomerService,
    meta: { keepAlive: true }
  },
  {
    path: '/monitor',
    name: 'monitor',
    component: SystemMonitor
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.meta.public && isAuthenticated()) {
    return { name: 'governance' }
  }

  if (!to.meta.public && !isAuthenticated()) {
    return {
      name: 'login',
      query: { redirect: to.fullPath }
    }
  }

  return true
})

export default router
