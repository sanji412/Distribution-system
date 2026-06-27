<template>
  <div class="app-shell">
    <AppHeader :pages="pages" :active-page="activePage" @change-page="activePage = $event" />
    <SideRail :pages="pages" :active-page="activePage" @change-page="activePage = $event" />

    <main class="app-main">
      <component :is="currentPage.component" />
    </main>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { BarChart3, ClipboardList, MonitorCog, RadioTower, Bot } from 'lucide-vue-next'
import AppHeader from './components/AppHeader.vue'
import SideRail from './components/SideRail.vue'
import ServiceGovernance from './views/ServiceGovernance.vue'
import OrderFulfillment from './views/OrderFulfillment.vue'
import DataDashboard from './views/DataDashboard.vue'
import AiCustomerService from './views/AiCustomerService.vue'
import SystemMonitor from './views/SystemMonitor.vue'

const pages = [
  {
    id: 'governance',
    label: '服务治理',
    title: '微服务治理中心',
    subtitle: 'Nacos / Gateway / Sentinel',
    icon: RadioTower,
    component: ServiceGovernance
  },
  {
    id: 'orders',
    label: '订单履约',
    title: '订单履约链路',
    subtitle: 'Order / Stock / Pay',
    icon: ClipboardList,
    component: OrderFulfillment
  },
  {
    id: 'dashboard',
    label: '数据看板',
    title: '经营分析驾驶舱',
    subtitle: 'Metrics / Trends / Status',
    icon: BarChart3,
    component: DataDashboard
  },
  {
    id: 'ai',
    label: 'AI客服',
    title: 'DeepSeek AI 智能客服',
    subtitle: 'Chat / Recommend / Insight',
    icon: Bot,
    component: AiCustomerService
  },
  {
    id: 'monitor',
    label: '系统监控',
    title: '系统运行监控',
    subtitle: 'Runtime / Versions / Knowledge',
    icon: MonitorCog,
    component: SystemMonitor
  }
]

const activePage = ref('orders')
const currentPage = computed(() => pages.find((page) => page.id === activePage.value) || pages[0])
</script>
