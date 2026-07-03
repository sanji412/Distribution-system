<template>
  <main class="login-page">
    <section class="login-panel">
      <div class="login-brand">
        <span class="status-dot"></span>
        <span class="brand-mark">⌁</span>
        <strong>智能分销平台监控</strong>
      </div>

      <h1>统一认证登录</h1>
      <p>登录后进入智能分销平台运行监控。</p>

      <form class="login-form" @submit.prevent="submitLogin">
        <label>
          <span>账号</span>
          <input v-model.trim="form.username" autocomplete="username" placeholder="admin" />
        </label>
        <label>
          <span>密码</span>
          <input
            v-model="form.password"
            autocomplete="current-password"
            placeholder="123456"
            type="password"
          />
        </label>
        <button type="submit" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <div class="login-hints">
        <span>统一入口</span>
        <span>安全会话</span>
        <span>默认账号 admin / 123456</span>
      </div>
    </section>
  </main>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { saveAuthSession } from '../api/session'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const form = reactive({
  username: 'admin',
  password: '123456'
})

async function submitLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }

  loading.value = true
  try {
    const loginResponse = await login(form)
    saveAuthSession(loginResponse)
    ElMessage.success('登录成功')
    router.replace(String(route.query.redirect || '/governance'))
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>
