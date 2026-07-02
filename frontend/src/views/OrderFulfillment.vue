<template>
  <section class="page-stack">
    <h1 class="page-title">📋 订单履约链路</h1>

    <div class="metric-grid">
      <MetricCard title="今日订单" :value="formatNumber(dashboard.summary.todayOrderCount)" tone="success" hint="实时统计" />
      <MetricCard title="今日成交额" :value="formatAmount(dashboard.summary.todayTradeAmount)" tone="primary" hint="已支付口径" />
      <MetricCard title="待发货" :value="dashboard.summary.pendingDeliveryCount" tone="warning" hint="待处理" />
      <MetricCard title="异常订单" :value="dashboard.summary.exceptionOrderCount" tone="danger" hint="异常状态" />
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

    <PanelBox title="分布式事务链路（Seata AT模式）">
      <template #action>
        <StatusTag v-if="seataWarning" value="演示数据" type="warning" />
        <StatusTag v-else value="真实记录" type="success" />
      </template>
      <p v-if="seataWarning" class="inline-warning">{{ seataWarning }}</p>
      <div class="seata-metric-grid" v-if="seataFlow.metrics?.length">
        <article v-for="metric in seataFlow.metrics" :key="metric.name" class="seata-metric">
          <span>{{ metric.name }}</span>
          <strong :class="metric.type">{{ metric.value }}</strong>
          <small>{{ metric.desc }}</small>
        </article>
      </div>
      <div class="flow-chain">
        <template v-for="(step, index) in seataFlow.steps" :key="step.name">
          <article class="flow-node" :class="`flow-${step.type}`">
            <strong>{{ step.name }}</strong>
            <span>{{ step.desc }}</span>
          </article>
          <span v-if="index < seataFlow.steps.length - 1" class="flow-arrow">→</span>
        </template>
      </div>
      <div class="seata-two-column">
        <div class="table-wrap">
          <table class="data-table compact-table">
            <thead>
              <tr>
                <th>数据库</th>
                <th>服务</th>
                <th>undo_log</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in seataFlow.undoLogs" :key="item.databaseName">
                <td>{{ item.databaseName }}</td>
                <td><StatusTag :value="item.serviceName" /></td>
                <td>{{ item.count }}</td>
                <td><StatusTag :value="item.status" :type="item.status === '可读取' ? 'success' : 'danger'" /></td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="table-wrap">
          <table class="data-table compact-table">
            <thead>
              <tr>
                <th>订单号</th>
                <th>XID</th>
                <th>全局状态</th>
                <th>库存分支</th>
                <th>支付分支</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="record in seataFlow.records" :key="`${record.orderNo}-${record.xid}`">
                <td>{{ record.orderNo }}</td>
                <td class="mono-cell">{{ shortXid(record.xid) }}</td>
                <td><StatusTag :value="record.transactionStatus" /></td>
                <td><StatusTag :value="record.stockBranchStatus" /></td>
                <td><StatusTag :value="record.payBranchStatus" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="transaction-log">
        <p v-for="line in seataFlow.logs" :key="line.text" :class="line.type">
          {{ logPrefix(line.type) }} {{ line.text }}
        </p>
      </div>
    </PanelBox>

    <PanelBox title="Sentinel限流监控">
      <template #action>
        <StatusTag v-if="sentinelWarning" value="演示数据" type="warning" />
        <StatusTag v-else value="已联调" type="success" />
      </template>
      <p v-if="sentinelWarning" class="inline-warning">{{ sentinelWarning }}</p>
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>资源名</th>
              <th>限流阈值</th>
              <th>当前QPS</th>
              <th>拒绝QPS</th>
              <th>熔断状态</th>
              <th>降级策略</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="rule in sentinelRules" :key="rule.resource">
              <td>{{ rule.resource }}</td>
              <td>{{ rule.threshold }}</td>
              <td>{{ rule.qps }}</td>
              <td>{{ rule.blocked }}</td>
              <td><StatusTag :value="rule.status" /></td>
              <td>{{ rule.strategy }}</td>
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
import { mockOrderDashboard, seataFlowMock, sentinelRulesMock } from '../api/mock'
import { getOrderDashboard, getSeataFlow } from '../api/order'
import { listSentinelRules } from '../api/stock'

const dashboard = ref(mockOrderDashboard)
const seataFlow = ref(seataFlowMock)
const sentinelRules = ref(sentinelRulesMock)
const loading = ref(false)
const warning = ref('')
const seataWarning = ref('')
const sentinelWarning = ref('')

onMounted(async () => {
  loading.value = true
  warning.value = ''
  seataWarning.value = ''
  sentinelWarning.value = ''

  try {
    dashboard.value = await getOrderDashboard()
  } catch (error) {
    dashboard.value = mockOrderDashboard
    warning.value = '订单服务未启动，当前展示本地演示数据。'
  } finally {
    seataFlow.value = await loadSeataFlow()
    sentinelRules.value = await loadSentinelRules()
    loading.value = false
  }
})

async function loadSeataFlow() {
  try {
    const flow = await getSeataFlow()
    if (flow?.steps?.length && flow?.logs?.length) {
      return flow
    }
    seataWarning.value = 'Seata 链路接口暂无数据，当前展示本地演示数据。'
    return seataFlowMock
  } catch (error) {
    seataWarning.value = '订单服务 Seata 链路接口未启动，当前展示本地演示数据。'
    return seataFlowMock
  }
}

async function loadSentinelRules() {
  try {
    const rules = await listSentinelRules()
    return rules?.length ? rules : sentinelRulesMock
  } catch (error) {
    sentinelWarning.value = '库存服务 Sentinel 接口未启动，当前展示本地演示数据。'
    return sentinelRulesMock
  }
}

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

function logPrefix(type) {
  if (type === 'success') {
    return '✅'
  }
  if (type === 'warning') {
    return '⚠️'
  }
  return '❌'
}

function shortXid(xid) {
  if (!xid || xid === '-') {
    return '-'
  }
  return xid.length > 18 ? `${xid.slice(0, 18)}...` : xid
}
</script>
