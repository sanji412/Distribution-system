import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api/order': {
        target: 'http://localhost:9000',
        changeOrigin: true
      },
      '/api/user': {
        target: 'http://localhost:9000',
        changeOrigin: true
      },
      '/api/product': {
        target: 'http://localhost:9000',
        changeOrigin: true
      },
      '/api/stock': {
        target: 'http://localhost:9000',
        changeOrigin: true
      },
      '/api/pay': {
        target: 'http://localhost:9000',
        changeOrigin: true
      },
      '/api/governance': {
        target: 'http://localhost:9000',
        changeOrigin: true
      }
    }
  }
})
