<template>
  <section class="page-stack">
    <h1 class="page-title">经营分析驾驶舱</h1>

    <div class="metric-grid">
      <MetricCard title="今日订单" value="1,280" tone="success" hint="↑ 12%" />
      <MetricCard title="成交额" value="¥128K" tone="primary" hint="↑ 8%" />
      <MetricCard title="待发货" value="23" tone="warning" hint="需处理" />
      <MetricCard title="异常" value="5" tone="danger" hint="库存不足" />
    </div>

    <div class="split-grid">
      <PanelBox title="今日各服务调用量">
        <div class="bar-chart service-bars">
          <div v-for="item in dashboardMock.serviceCalls" :key="item.name" class="bar-item">
            <div class="bar-fill" :style="{ height: `${item.value / 10}px` }"></div>
            <span>{{ item.name }}</span>
          </div>
        </div>
      </PanelBox>

      <PanelBox title="订单状态分布">
        <div class="pie-layout">
          <div class="pie-chart"></div>
          <div class="legend-row">
            <span v-for="item in dashboardMock.status" :key="item.label">
              <i :style="{ background: item.color }"></i>{{ item.label }} {{ item.percent }}%
            </span>
          </div>
        </div>
      </PanelBox>
    </div>

    <PanelBox title="近7天订单趋势">
      <div class="bar-chart trend-bars">
        <div v-for="item in dashboardMock.trend" :key="item.date" class="bar-item">
          <div class="bar-fill" :style="{ height: `${item.value / 6}px` }"></div>
          <span>{{ item.date }}</span>
        </div>
      </div>
    </PanelBox>
  </section>
</template>

<script setup>
import MetricCard from '../components/MetricCard.vue'
import PanelBox from '../components/PanelBox.vue'
import { dashboardMock } from '../api/mock'
</script>
