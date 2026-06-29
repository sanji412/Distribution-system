<template>
  <section class="page-stack">
    <h1 class="page-title">📊 经营分析驾驶舱</h1>

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
            <div class="bar-fill" :style="{ height: `${item.value / 5.5}px` }"></div>
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

    <div class="split-grid data-ops-grid">
      <PanelBox title="商品管理（CRUD）">
        <template #action>
          <div class="mini-actions">
            <StatusTag v-if="loading" value="加载中" />
            <StatusTag v-else-if="warning" value="演示数据" type="warning" />
            <StatusTag v-else value="已联调" type="success" />
            <button type="button">新增</button>
            <button type="button">导出</button>
          </div>
        </template>
        <p v-if="warning" class="inline-warning">{{ warning }}</p>
        <div class="table-wrap">
          <table class="data-table compact-table">
            <thead>
              <tr>
                <th>商品编码</th>
                <th>商品名称</th>
                <th>分类</th>
                <th>价格</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="product in products" :key="product.code">
                <td>{{ product.code }}</td>
                <td>{{ product.name }}</td>
                <td>{{ product.category }}</td>
                <td>¥{{ product.price }}</td>
                <td><StatusTag :value="product.status" /></td>
                <td>
                  <button class="text-action" type="button">编辑</button>
                  <button class="text-action danger" type="button">下架</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </PanelBox>

      <PanelBox title="库存查询">
        <template #action>
          <StatusTag v-if="loading" value="加载中" />
          <StatusTag v-else-if="warning" value="演示数据" type="warning" />
          <StatusTag v-else value="Gateway联调" type="primary" />
        </template>
        <div class="table-wrap">
          <table class="data-table compact-table">
            <thead>
              <tr>
                <th>仓库</th>
                <th>商品</th>
                <th>可用</th>
                <th>锁定</th>
                <th>库存服务</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="stock in stocks" :key="`${stock.warehouse}-${stock.product}`">
                <td>{{ stock.warehouse }}</td>
                <td>{{ stock.product }}</td>
                <td>{{ stock.available }}</td>
                <td>{{ stock.locked }}</td>
                <td><StatusTag :value="stock.service" /></td>
                <td><StatusTag :value="stock.status" /></td>
              </tr>
            </tbody>
          </table>
        </div>
      </PanelBox>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import MetricCard from '../components/MetricCard.vue'
import PanelBox from '../components/PanelBox.vue'
import StatusTag from '../components/StatusTag.vue'
import { dashboardMock, productManagementMock, stockQueryMock } from '../api/mock'
import { listProducts } from '../api/product'
import { listStocks, listWarehouses } from '../api/stock'

const products = ref(productManagementMock)
const stocks = ref(stockQueryMock)
const loading = ref(false)
const warning = ref('')

onMounted(async () => {
  loading.value = true
  warning.value = ''

  try {
    const [productRows, stockRows, warehouseRows] = await Promise.all([
      listProducts(),
      listStocks(),
      listWarehouses()
    ])

    const productMap = new Map(productRows.map((item) => [item.productId, item]))
    const warehouseMap = new Map(warehouseRows.map((item) => [item.warehouseId, item]))

    products.value = productRows.map((item) => ({
      code: item.skuCode || `P${String(item.productId).padStart(12, '0')}`,
      name: item.productName,
      category: item.category || '未分类',
      price: item.price,
      status: item.status === 1 ? '上架' : '缺货'
    }))

    stocks.value = stockRows.map((item) => {
      const stockNum = Number(item.stockNum || 0)
      const product = productMap.get(item.productId)
      const warehouse = warehouseMap.get(item.warehouseId)

      return {
        warehouse: warehouse?.warehouseName || `仓库${item.warehouseId}`,
        product: product?.productName || `商品${item.productId}`,
        available: stockNum,
        locked: 0,
        service: Number(item.warehouseId) % 2 === 0 ? 'stock:8004' : 'stock:8003',
        status: stockNum <= 0 ? '缺货' : stockNum <= 10 ? '预警' : '正常'
      }
    })
  } catch (error) {
    products.value = productManagementMock
    stocks.value = stockQueryMock
    warning.value = '商品或库存服务未启动，当前展示本地演示数据。'
  } finally {
    loading.value = false
  }
})
</script>
