<template>
  <span class="status-tag" :class="tagClass">{{ value }}</span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: {
    type: String,
    required: true
  },
  type: {
    type: String,
    default: ''
  }
})

const tagClass = computed(() => {
  if (props.type) {
    return `tag-${props.type}`
  }
  if (['已支付', '已发货', '已完成', '正常', '上架', '已联调', '运行中', '在线'].includes(props.value)) {
    return 'tag-success'
  }
  if (['待支付', '警告', '预警', '演示数据', '加载中'].includes(props.value)) {
    return 'tag-warning'
  }
  if (['库存不足', '扣减失败', '异常', '缺货'].includes(props.value)) {
    return 'tag-danger'
  }
  if (props.value.includes(':')) {
    return 'tag-service'
  }
  return 'tag-muted'
})
</script>
