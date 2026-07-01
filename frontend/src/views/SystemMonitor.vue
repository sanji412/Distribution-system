<template>
  <section class="page-stack">
    <h1 class="page-title">⚙️ 系统运行监控</h1>

    <p v-if="warning" class="inline-warning">{{ warning }}</p>

    <div class="monitor-card-grid">
      <MetricCard
        v-for="item in monitor.cards"
        :key="item.title"
        :title="item.title"
        :value="item.value"
        :tone="item.tone"
      />
    </div>

    <PanelBox title="系统运行指标">
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>指标</th>
              <th>数值</th>
              <th>说明</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in monitor.rows" :key="row.metric">
              <td>{{ row.metric }}</td>
              <td>{{ row.value }}</td>
              <td>{{ row.description }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </PanelBox>

    <PanelBox title="实训待接知识点映射">
      <ul class="knowledge-list">
        <li v-for="item in monitor.knowledge" :key="item">{{ item }}</li>
      </ul>
    </PanelBox>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import MetricCard from '../components/MetricCard.vue'
import PanelBox from '../components/PanelBox.vue'
import { getSystemMonitor } from '../api/governance'
import { monitorMock } from '../api/mock'

const monitor = ref(toMonitorShape(monitorMock))
const warning = ref('')

onMounted(loadSystemMonitor)

async function loadSystemMonitor() {
  warning.value = ''
  try {
    monitor.value = toMonitorShape(await getSystemMonitor())
  } catch (error) {
    monitor.value = toMonitorShape(monitorMock)
    warning.value = '系统监控接口未启动，当前展示本地演示数据。'
  }
}

function toMonitorShape(data) {
  return {
    cards: data?.cards || monitorMock.cards,
    rows: (data?.rows || monitorMock.rows).map((row) => Array.isArray(row)
      ? { metric: row[0], value: row[1], description: row[2] }
      : row
    ),
    knowledge: data?.knowledge || monitorMock.knowledge
  }
}
</script>
