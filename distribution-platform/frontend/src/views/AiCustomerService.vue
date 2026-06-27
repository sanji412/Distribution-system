<template>
  <section class="page-stack">
    <h1 class="page-title">DeepSeek AI 智能客服</h1>

    <PanelBox title="客服对话窗口">
      <template #action>
        <StatusTag value="在线" type="success" />
      </template>
      <div class="chat-window">
        <div v-for="(message, index) in aiMock.messages" :key="index" class="chat-row" :class="message.role">
          <span class="avatar">{{ message.role === 'user' ? 'U' : 'AI' }}</span>
          <p>{{ message.content }}</p>
        </div>
      </div>
      <div class="chat-input-row">
        <input type="text" placeholder="输入问题..." />
        <button type="button">发送</button>
      </div>
    </PanelBox>

    <PanelBox title="AI智能推荐（模拟）">
      <div class="recommend-grid">
        <article v-for="item in aiMock.recommendations" :key="item.title" class="recommend-card">
          <strong>{{ item.title }}</strong>
          <p>{{ item.content }}</p>
        </article>
      </div>
    </PanelBox>
  </section>
</template>

<script setup>
import PanelBox from '../components/PanelBox.vue'
import StatusTag from '../components/StatusTag.vue'
import { aiMock } from '../api/mock'
</script>
