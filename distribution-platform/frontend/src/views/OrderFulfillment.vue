<template>
  <section class="page-stack">
    <h1 class="page-title">订单履约链路</h1>

    <div class="metric-grid">
      <MetricCard title="今日订单" :value="formatNumber(dashboard.summary.todayOrderCount)" tone="success" hint="↑ 12%" />
      <MetricCard title="今日成交额" :value="formatAmount(dashboard.summary.todayTradeAmount)" tone="primary" hint="↑ 8%" />
      <MetricCard title="待发货" :value="dashboard.summary.pendingDeliveryCount" tone="warning" hint="需处理" />
      <MetricCard title="异常订单" :value="dashboard.summary.exceptionOrderCount" tone="danger" hint="库存不足" />
    </div>

    <PanelBox title="订单列表（跨服务调用）">
      <template #action>
        <StatusTag v-if="loading" value="加载中" />
        <StatusTag v-else-if="warning" value="演示数据" type="warning" />
        <StatusTag v-else value="已联调" type="success" />
      </template>
      <p v-if="warning" class="inline-warning">{{ warning }}</p>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>订单号</th>
              <th>商品</th>
              <th>数量</th>
              <th>金额</th>
              <th>状态</th>
              <th>库存服务</th>
              <th>支付服务</th>
              <th>时间</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in dashboard.orders" :key="order.orderNo">
              <td>{{ order.orderNo }}</td>
              <td>{{ order.productName }}</td>
              <td>{{ order.productNum }}</td>
              <td>¥{{ order.totalAmount }}</td>
              <td><StatusTag :value="order.orderStatus" /></td>
              <td><StatusTag :value="order.stockService" /></td>
              <td><StatusTag :value="order.payService" /></td>
              <td>{{ order.orderTime }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </PanelBox>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import MetricCard from '../components/MetricCard.vue'
import PanelBox from '../components/PanelBox.vue'
import StatusTag from '../components/StatusTag.vue'
import { mockOrderDashboard } from '../api/mock'
import { getOrderDashboard } from '../api/order'

const dashboard = ref(mockOrderDashboard)
const loading = ref(false)
const warning = ref('')

onMounted(async () => {
  loading.value = true
  warning.value = ''

  try {
    dashboard.value = await getOrderDashboard()
  } catch (error) {
    dashboard.value = mockOrderDashboard
    warning.value = '订单服务未启动，当前展示本地演示数据。'
  } finally {
    loading.value = false
  }
})

function formatNumber(value) {
  return Number(value).toLocaleString('en-US')
}

function formatAmount(value) {
  const amount = Number(value)
  if (amount >= 1000) {
    return `¥${Math.round(amount / 1000)}K`
  }
  return `¥${amount}`
}
</script>
