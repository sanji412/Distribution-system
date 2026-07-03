<template>
  <section class="page-stack">
    <h1 class="page-title">🤖 DeepSeek AI 智能客服</h1>

    <PanelBox title="客服对话窗口">
      <template #action>
        <StatusTag :value="modelStatus" :type="modelStatusType" />
      </template>
      <div ref="chatWindowRef" class="chat-window">
        <div v-for="message in messages" :key="message.id" class="chat-row" :class="message.role">
          <span class="avatar">{{ message.role === 'user' ? 'U' : message.role === 'system' ? 'S' : 'AI' }}</span>
          <p>{{ message.content }}</p>
        </div>
      </div>
      <div class="chat-input-row">
        <input
          v-model="inputMessage"
          type="text"
          placeholder="输入问题，例如：我的订单DD202507010001到哪了？"
          @keyup.enter="handleSend"
        />
        <button type="button" :disabled="sending" @click="handleSend">{{ sending ? '...' : '发送' }}</button>
      </div>
      <div v-if="lastResult" class="ai-meta">
        <span>意图：{{ lastResult.intent }}</span>
        <span>数据源：{{ lastResult.dataSource }}</span>
        <span>模型：{{ lastResult.modelUsed }}</span>
      </div>
    </PanelBox>

    <PanelBox title="AI智能推荐（浏览历史）">
      <template #action>
        <StatusTag :value="recommendationStatus" :type="recommendationStatusType" />
      </template>
      <div v-if="recommendationWarning" class="inline-warning">{{ recommendationWarning }}</div>
      <div class="recommend-grid">
        <article v-for="item in recommendations" :key="`${item.title}-${item.content}`" class="recommend-card">
          <strong>{{ item.title }}</strong>
          <p>{{ item.content }}</p>
        </article>
      </div>
      <div v-if="recommendationResult" class="ai-meta">
        <span>数据源：{{ recommendationResult.dataSource }}</span>
        <span>模型：{{ recommendationResult.modelUsed }}</span>
        <span>用户：{{ recommendationResult.userId }}</span>
      </div>
    </PanelBox>

    <PanelBox title="Prompt模板映射">
      <template #action>
        <StatusTag value="基于Controller生成" type="primary" />
      </template>
      <div class="recommend-grid">
        <article v-for="item in promptTemplate?.intents || []" :key="item.intent" class="recommend-card">
          <strong>{{ item.name }}</strong>
          <p>{{ item.intent }} · {{ item.requiredSlots }}</p>
          <p>{{ item.api }}</p>
        </article>
      </div>
      <div class="prompt-tabs">
        <button
          v-for="tab in promptTabs"
          :key="tab.key"
          type="button"
          :class="{ active: activePromptKey === tab.key }"
          @click="activePromptKey = tab.key"
        >
          {{ tab.label }}
        </button>
      </div>
      <pre class="prompt-box">{{ activePrompt }}</pre>
    </PanelBox>
  </section>
</template>

<script setup>
import { computed, nextTick, onActivated, onMounted, ref } from 'vue'
import PanelBox from '../components/PanelBox.vue'
import StatusTag from '../components/StatusTag.vue'
import { getAiPromptTemplate, getAiRecommendations, sendAiMessage } from '../api/ai'
import { resolveAiModelStatus, resolveRecommendationStatus } from '../utils/aiStatus'

const messages = ref([
  {
    id: 1,
    role: 'ai',
    content: '亲亲你好，我是 DeepSeek AI 客服，可以帮你查询订单物流、商品库存和商品信息。'
  }
])
const inputMessage = ref('')
const sending = ref(false)
const promptTemplate = ref(null)
const activePromptKey = ref('intentRecognitionPrompt')
const lastResult = ref(null)
const chatWindowRef = ref(null)
const recommendationResult = ref(null)
const recommendationWarning = ref('')
const recommendationLoading = ref(false)

const promptTabs = [
  { key: 'intentRecognitionPrompt', label: '意图识别' },
  { key: 'customerServicePrompt', label: '客服基础' },
  { key: 'orderLogisticsPrompt', label: '物流动态' },
  { key: 'stockPrompt', label: '库存动态' }
]

const activePrompt = computed(() => {
  if (!promptTemplate.value) {
    return 'Prompt 模板加载中...'
  }
  return promptTemplate.value[activePromptKey.value] || '暂无模板'
})

const modelStatus = computed(() => {
  return resolveAiModelStatus({
    lastResult: lastResult.value,
    recommendationResult: recommendationResult.value,
    loading: recommendationLoading.value
  }).label
})

const modelStatusType = computed(() => resolveAiModelStatus({
  lastResult: lastResult.value,
  recommendationResult: recommendationResult.value,
  loading: recommendationLoading.value
}).type)
const recommendations = computed(() => recommendationResult.value?.items || [])
const recommendationStatus = computed(() => {
  return resolveRecommendationStatus({
    loading: recommendationLoading.value,
    recommendationResult: recommendationResult.value
  }).label
})
const recommendationStatusType = computed(() => resolveRecommendationStatus({
  loading: recommendationLoading.value,
  recommendationResult: recommendationResult.value
}).type)

onMounted(async () => {
  try {
    promptTemplate.value = await getAiPromptTemplate()
  } catch (error) {
    messages.value.push({
      id: Date.now(),
      role: 'system',
      content: `Prompt 模板加载失败：${error.message}`
    })
  }

  await loadRecommendations()
})

onActivated(async () => {
  if (recommendationWarning.value || recommendationResult.value?.modelUsed === 'local-fallback') {
    await loadRecommendations()
  }
})

async function loadRecommendations() {
  if (recommendationLoading.value) {
    return
  }

  recommendationLoading.value = true
  recommendationWarning.value = ''
  try {
    recommendationResult.value = await getAiRecommendations(1)
  } catch (error) {
    recommendationWarning.value = `智能推荐接口请求失败：${error.message}`
    recommendationResult.value = {
      userId: 1,
      dataSource: 'local-fallback',
      modelUsed: 'local-fallback',
      items: [
        {
          title: '推荐暂不可用',
          content: '智能推荐依赖 order-service 和 product-service 联调，请确认服务已启动后刷新页面。'
        }
      ]
    }
  } finally {
    recommendationLoading.value = false
  }
}

async function handleSend() {
  const content = inputMessage.value.trim()
  if (!content || sending.value) {
    return
  }

  messages.value.push({ id: Date.now(), role: 'user', content })
  inputMessage.value = ''
  sending.value = true
  await scrollToBottom()

  try {
    const result = await sendAiMessage({
      userId: 1,
      message: content
    })
    lastResult.value = result
    messages.value.push({
      id: Date.now() + 1,
      role: 'ai',
      content: result.reply
    })
  } catch (error) {
    messages.value.push({
      id: Date.now() + 1,
      role: 'system',
      content: `AI 客服接口请求失败：${error.message}`
    })
  } finally {
    sending.value = false
    await scrollToBottom()
  }
}

async function scrollToBottom() {
  await nextTick()
  if (chatWindowRef.value) {
    chatWindowRef.value.scrollTop = chatWindowRef.value.scrollHeight
  }
}
</script>
