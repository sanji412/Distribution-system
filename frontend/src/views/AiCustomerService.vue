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
import { computed, nextTick, onMounted, ref } from 'vue'
import PanelBox from '../components/PanelBox.vue'
import StatusTag from '../components/StatusTag.vue'
import { getAiPromptTemplate, sendAiMessage } from '../api/ai'

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
  if (!lastResult.value) {
    return '在线'
  }
  return lastResult.value.modelUsed === 'local-fallback' ? '降级可用' : 'DeepSeek在线'
})

const modelStatusType = computed(() => (lastResult.value?.modelUsed === 'local-fallback' ? 'warning' : 'success'))

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
})

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
