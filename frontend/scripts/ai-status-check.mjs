import {
  resolveAiModelStatus,
  resolveRecommendationStatus
} from '../src/utils/aiStatus.js'

function assertEqual(actual, expected, message) {
  if (actual !== expected) {
    throw new Error(`${message}: expected ${expected}, got ${actual}`)
  }
}

assertEqual(resolveAiModelStatus({}).label, '待检测', 'empty chat status label')
assertEqual(resolveAiModelStatus({ loading: true }).label, '检测中', 'loading chat status label')
assertEqual(
  resolveAiModelStatus({ recommendationResult: { modelUsed: 'local-fallback' } }).label,
  '降级可用',
  'fallback recommendation should drive chat status before first chat'
)
assertEqual(
  resolveAiModelStatus({ lastResult: { modelUsed: 'deepseek-chat' } }).label,
  'DeepSeek在线',
  'chat result should drive chat status'
)
assertEqual(
  resolveRecommendationStatus({ loading: false, recommendationResult: { modelUsed: 'local-fallback' } }).label,
  '本地推荐',
  'fallback recommendation label'
)
assertEqual(
  resolveRecommendationStatus({ loading: false, recommendationResult: { modelUsed: 'deepseek-chat' } }).label,
  'DeepSeek推荐',
  'DeepSeek recommendation label'
)

console.log('[ok] ai status checks passed')
