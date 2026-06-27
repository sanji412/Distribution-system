<template>
  <aside class="side-rail" aria-label="侧边导航">
    <button
      v-for="item in pages"
      :key="item.id"
      class="rail-button"
      :class="{ active: item.id === activePage }"
      type="button"
      :title="item.label"
      @click="$emit('change-page', item.id)"
    >
      <component :is="item.icon" :size="21" :stroke-width="2.2" />
    </button>
  </aside>
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
