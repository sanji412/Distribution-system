<template>
  <header class="app-header">
    <div class="brand">
      <span class="status-dot"></span>
      <span class="brand-mark">DP</span>
      <strong>智能分销平台监控</strong>
    </div>

    <nav class="top-nav" aria-label="主导航">
      <button
        v-for="item in pages"
        :key="item.id"
        class="nav-pill"
        :class="{ active: item.id === activePage }"
        type="button"
        @click="$emit('change-page', item.id)"
      >
        {{ item.label }}
      </button>
    </nav>

    <div class="account">
      <span class="admin-badge">{{ roleLabel }}</span>
      <span>{{ displayName }}</span>
      <button class="logout-button" type="button" @click="$emit('logout')">退出</button>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  pages: {
    type: Array,
    required: true
  },
  activePage: {
    type: String,
    required: true
  },
  currentUser: {
    type: Object,
    default: null
  }
})

defineEmits(['change-page', 'logout'])

const displayName = computed(() => props.currentUser?.nickname || props.currentUser?.username || '未登录')
const roleLabel = computed(() => String(props.currentUser?.role || 'USER').toUpperCase())
</script>
