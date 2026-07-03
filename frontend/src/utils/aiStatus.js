const FALLBACK_MODEL = 'local-fallback'

export function resolveAiModelStatus({ lastResult, recommendationResult, loading = false } = {}) {
  const source = lastResult || recommendationResult

  if (!source) {
    return loading
      ? { label: '检测中', type: 'primary' }
      : { label: '待检测', type: 'primary' }
  }

  return source.modelUsed === FALLBACK_MODEL
    ? { label: '降级可用', type: 'warning' }
    : { label: 'DeepSeek在线', type: 'success' }
}

export function resolveRecommendationStatus({ loading, recommendationResult } = {}) {
  if (loading || !recommendationResult) {
    return { label: '加载中', type: 'primary' }
  }

  return recommendationResult.modelUsed === FALLBACK_MODEL
    ? { label: '本地推荐', type: 'warning' }
    : { label: 'DeepSeek推荐', type: 'success' }
}
