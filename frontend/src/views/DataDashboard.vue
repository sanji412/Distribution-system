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
            <button type="button" @click="openCreateProduct">新增</button>
            <button type="button" @click="exportProducts">导出</button>
          </div>
        </template>
        <p v-if="warning" class="inline-warning">{{ warning }}</p>
        <p v-if="productMessage" class="inline-warning success">{{ productMessage }}</p>
        <form v-if="productEditorVisible" class="product-editor" @submit.prevent="saveProduct">
          <label>
            <span>商品编码</span>
            <input v-model.trim="productForm.skuCode" type="text" placeholder="SKU-K3-001" />
          </label>
          <label>
            <span>商品名称</span>
            <input v-model.trim="productForm.productName" type="text" placeholder="机械键盘-Keychron K3" />
          </label>
          <label>
            <span>分类</span>
            <input v-model.trim="productForm.category" type="text" placeholder="外设" />
          </label>
          <label>
            <span>价格</span>
            <input v-model.number="productForm.price" type="number" min="0" step="0.01" />
          </label>
          <label>
            <span>安全库存</span>
            <input v-model.number="productForm.safeStock" type="number" min="0" step="1" />
          </label>
          <label>
            <span>状态</span>
            <select v-model.number="productForm.status">
              <option :value="1">上架</option>
              <option :value="0">下架</option>
            </select>
          </label>
          <div class="product-editor-actions">
            <button type="submit" :disabled="savingProduct">保存</button>
            <button type="button" @click="cancelProductEdit">取消</button>
          </div>
        </form>
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
              <tr v-for="product in products" :key="product.productId || product.code">
                <td>{{ product.code }}</td>
                <td>{{ product.name }}</td>
                <td>{{ product.category }}</td>
                <td>¥{{ formatMoney(product.price) }}</td>
                <td><StatusTag :value="product.status" /></td>
                <td>
                  <button class="text-action" type="button" @click="openEditProduct(product)">编辑</button>
                  <button class="text-action danger" type="button" @click="removeProduct(product)">删除</button>
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
import { createProduct, deleteProduct, listProducts, updateProduct } from '../api/product'
import { listStocks, listWarehouses } from '../api/stock'

const products = ref(productManagementMock)
const stocks = ref(stockQueryMock)
const loading = ref(false)
const warning = ref('')
const productEditorVisible = ref(false)
const savingProduct = ref(false)
const productMessage = ref('')

const emptyProductForm = () => ({
  productId: null,
  skuCode: '',
  productName: '',
  category: '',
  price: 0,
  status: 1,
  safeStock: 0,
  description: ''
})

const productForm = ref(emptyProductForm())

onMounted(loadDashboardData)

async function loadDashboardData() {
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

    products.value = productRows.map(toProductRow)

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
}

function toProductRow(item) {
  return {
    productId: item.productId,
    code: item.skuCode || `P${String(item.productId).padStart(12, '0')}`,
    skuCode: item.skuCode || '',
    name: item.productName,
    productName: item.productName,
    category: item.category || '未分类',
    price: item.price,
    status: item.status === 1 ? '上架' : '下架',
    rawStatus: item.status ?? 1,
    safeStock: item.safeStock ?? 0,
    description: item.description || ''
  }
}

function openCreateProduct() {
  productMessage.value = ''
  productForm.value = emptyProductForm()
  productEditorVisible.value = true
}

function openEditProduct(product) {
  productMessage.value = ''
  productForm.value = {
    productId: product.productId,
    skuCode: product.skuCode || product.code,
    productName: product.productName || product.name,
    category: product.category === '未分类' ? '' : product.category,
    price: Number(product.price || 0),
    status: product.rawStatus ?? (product.status === '上架' ? 1 : 0),
    safeStock: product.safeStock ?? 0,
    description: product.description || ''
  }
  productEditorVisible.value = true
}

function cancelProductEdit() {
  productEditorVisible.value = false
  productForm.value = emptyProductForm()
}

async function saveProduct() {
  if (!productForm.value.productName) {
    productMessage.value = '请填写商品名称。'
    return
  }

  savingProduct.value = true
  productMessage.value = ''
  try {
    const payload = {
      productName: productForm.value.productName,
      category: productForm.value.category,
      price: Number(productForm.value.price || 0),
      skuCode: productForm.value.skuCode,
      status: Number(productForm.value.status),
      safeStock: Number(productForm.value.safeStock || 0),
      description: productForm.value.description
    }

    if (productForm.value.productId) {
      await updateProduct(productForm.value.productId, payload)
      productMessage.value = '商品信息已保存。'
    } else {
      await createProduct(payload)
      productMessage.value = '商品已新增。'
    }

    productEditorVisible.value = false
    productForm.value = emptyProductForm()
    await loadDashboardData()
  } catch (error) {
    productMessage.value = error.message || '商品保存失败。'
  } finally {
    savingProduct.value = false
  }
}

async function removeProduct(product) {
  if (!product.productId) {
    productMessage.value = '演示数据无法删除。'
    return
  }

  const confirmed = window.confirm(`确认删除商品「${product.name}」？`)
  if (!confirmed) {
    return
  }

  productMessage.value = ''
  try {
    await deleteProduct(product.productId)
    productMessage.value = '商品已删除。'
    await loadDashboardData()
  } catch (error) {
    productMessage.value = error.message || '商品删除失败。'
  }
}

function exportProducts() {
  const blob = new Blob([JSON.stringify(products.value, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = 'products.json'
  link.click()
  URL.revokeObjectURL(url)
}

function formatMoney(value) {
  return Number(value || 0).toFixed(2)
}
</script>
