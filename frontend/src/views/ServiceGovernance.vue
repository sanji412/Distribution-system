<template>
  <section class="page-stack">
    <h1 class="page-title">📡 微服务治理中心</h1>

    <div class="metric-grid">
      <MetricCard
        v-for="metric in governanceMock.metrics"
        :key="metric.title"
        :title="metric.title"
        :value="metric.value"
        :tone="metric.tone"
        :hint="metric.hint"
      />
    </div>

    <PanelBox title="Nacos服务注册日志">
      <template #action>
        <StatusTag value="运行中" type="success" />
      </template>
      <div class="terminal-box">
        <p v-for="line in governanceMock.logs" :key="line">
          <span :class="{ warn: line.includes('WARN'), info: line.includes('INFO') }">{{ line }}</span>
        </p>
      </div>
    </PanelBox>

    <PanelBox title="微服务实例状态">
      <div class="instance-grid">
        <article v-for="item in governanceMock.instances" :key="item.name" class="instance-item">
          <span class="health-dot"></span>
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
        <p v-for="line in governanceMock.routes" :key="line">{{ line }}</p>
      </div>
    </PanelBox>
  </section>
</template>

<script setup>
import MetricCard from '../components/MetricCard.vue'
import PanelBox from '../components/PanelBox.vue'
import StatusTag from '../components/StatusTag.vue'
import { governanceMock } from '../api/mock'
</script>
