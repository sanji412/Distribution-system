<template>
  <section class="page-stack">
    <h1 class="page-title">系统运行监控</h1>

    <div class="monitor-card-grid">
      <MetricCard
        v-for="item in monitorMock.cards"
        :key="item.title"
        :title="item.title"
        :value="item.value"
        :tone="item.tone"
      />
    </div>

    <PanelBox title="系统运行指标（模拟数据）">
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
            <tr v-for="row in monitorMock.rows" :key="row[0]">
              <td>{{ row[0] }}</td>
              <td>{{ row[1] }}</td>
              <td>{{ row[2] }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </PanelBox>

    <PanelBox title="实训待接知识点映射">
      <ul class="knowledge-list">
        <li v-for="item in monitorMock.knowledge" :key="item">{{ item }}</li>
      </ul>
    </PanelBox>
  </section>
</template>

<script setup>
import MetricCard from '../components/MetricCard.vue'
import PanelBox from '../components/PanelBox.vue'
import { monitorMock } from '../api/mock'
</script>
