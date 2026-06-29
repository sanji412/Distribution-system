<template>
  <article class="metric-card" :class="`metric-${tone}`">
    <p>{{ title }}</p>
    <strong>{{ value }}</strong>
    <span v-if="hint">{{ hint }}</span>
  </article>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    required: true
  },
  value: {
    type: [String, Number],
    required: true
  },
  tone: {
    type: String,
    default: 'primary'
  },
  hint: {
    type: String,
    default: ''
  }
})
</script>
