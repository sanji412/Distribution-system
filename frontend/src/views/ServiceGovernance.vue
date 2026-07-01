<template>
  <section class="page-stack">
    <h1 class="page-title">📡 微服务治理中心</h1>

    <div class="metric-grid">
      <MetricCard
        v-for="metric in governance.metrics"
        :key="metric.title"
        :title="metric.title"
        :value="metric.value"
        :tone="metric.tone"
        :hint="metric.hint"
      />
    </div>

    <PanelBox title="Nacos服务注册日志">
      <template #action>
        <StatusTag v-if="loading" value="加载中" />
        <StatusTag v-else-if="warning" value="演示数据" type="warning" />
        <StatusTag v-else :value="governance.status || '运行中'" type="success" />
      </template>
      <p v-if="warning" class="inline-warning">{{ warning }}</p>
      <div class="terminal-box">
        <p v-for="line in governance.logs" :key="line">
          <span :class="{ warn: line.includes('WARN'), info: line.includes('INFO') }">{{ line }}</span>
        </p>
      </div>
    </PanelBox>

    <PanelBox title="微服务实例状态">
      <div class="instance-grid">
        <article v-for="item in governance.instances" :key="item.name" class="instance-item">
          <span class="health-dot" :class="{ 'is-down': item.healthy === false }"></span>
          <div>
            <strong>{{ item.name }}</strong>
            <p>{{ item.desc }}</p>
          </div>
          <b>{{ item.latency }}</b>
        </article>
      </div>
    </PanelBox>

    <PanelBox title="Gateway路由配置">
      <div class="terminal-box route-box">
        <p v-for="line in governance.routes" :key="line">{{ line }}</p>
      </div>
    </PanelBox>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import MetricCard from '../components/MetricCard.vue'
import PanelBox from '../components/PanelBox.vue'
import StatusTag from '../components/StatusTag.vue'
import { getGovernanceOverview } from '../api/governance'
import { governanceMock } from '../api/mock'

const governance = ref({
  status: '运行中',
  ...governanceMock
})
const loading = ref(false)
const warning = ref('')

onMounted(loadGovernanceOverview)

async function loadGovernanceOverview() {
  loading.value = true
  warning.value = ''

  try {
    const response = await getGovernanceOverview()
    governance.value = {
      status: response.status || '运行中',
      metrics: response.metrics?.length ? response.metrics : governanceMock.metrics,
      logs: response.logs?.length ? response.logs : governanceMock.logs,
      instances: response.instances?.length ? response.instances : governanceMock.instances,
      routes: response.routes?.length ? response.routes : governanceMock.routes
    }
  } catch (error) {
    governance.value = {
      status: '演示数据',
      ...governanceMock
    }
    warning.value = 'Gateway 或 Nacos 未启动，当前展示本地演示数据。'
  } finally {
    loading.value = false
  }
}
</script>
