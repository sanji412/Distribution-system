<template>
  <div class="app-shell">
    <AppHeader :pages="pages" :active-page="activePage" @change-page="goToPage" />
    <SideRail :pages="pages" :active-page="activePage" @change-page="goToPage" />

    <main class="app-main">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { BarChart3, ClipboardList, MonitorCog, RadioTower, Bot } from 'lucide-vue-next'
import AppHeader from './components/AppHeader.vue'
import SideRail from './components/SideRail.vue'

const route = useRoute()
const router = useRouter()

const pages = [
  {
    id: 'governance',
    path: '/governance',
    label: '服务治理',
    title: '微服务治理中心',
    subtitle: 'Nacos / Gateway / Sentinel',
    icon: RadioTower
  },
  {
    id: 'orders',
    path: '/orders',
    label: '订单履约',
    title: '订单履约链路',
    subtitle: 'Order / Stock / Pay',
    icon: ClipboardList
  },
  {
    id: 'dashboard',
    path: '/dashboard',
    label: '数据看板',
    title: '经营分析驾驶舱',
    subtitle: 'Metrics / Trends / Status',
    icon: BarChart3
  },
  {
    id: 'ai',
    path: '/ai',
    label: 'AI客服',
    title: 'DeepSeek AI 智能客服',
    subtitle: 'Chat / Recommend / Insight',
    icon: Bot
  },
  {
    id: 'monitor',
    path: '/monitor',
    label: '系统监控',
    title: '系统运行监控',
    subtitle: 'Runtime / Versions / Knowledge',
    icon: MonitorCog
  }
]

const activePage = computed(() => String(route.name || 'governance'))

function goToPage(pageId) {
  if (pageId !== activePage.value) {
    router.push({ name: pageId })
  }
}
</script>
