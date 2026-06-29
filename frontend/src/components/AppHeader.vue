<template>
  <header class="app-header">
    <div class="brand">
      <span class="status-dot"></span>
      <span class="brand-mark">⌁</span>
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
      <span class="admin-badge">ADMIN</span>
      <span>admin</span>
    </div>
  </header>
</template>

<script setup>
defineProps({
  pages: {
    type: Array,
    required: true
  },
  activePage: {
    type: String,
    required: true
  }
})

defineEmits(['change-page'])
</script>
