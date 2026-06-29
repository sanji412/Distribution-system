<template>
  <section class="panel-box">
    <header v-if="title" class="panel-header">
      <h3>{{ title }}</h3>
      <slot name="action"></slot>
    </header>
    <div class="panel-body">
      <slot></slot>
    </div>
  </section>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  }
})
</script>
