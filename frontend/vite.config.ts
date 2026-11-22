/// <reference types="vitest" />
import { defineConfig } from 'vitest/config'
import react from '@vitejs/plugin-react'

// Em desenvolvimento, as chamadas /api/* são encaminhadas ao backend (mesma origem para o cookie de sessão).
const backend = process.env.VITE_PROXY_TARGET ?? 'http://localhost:8090'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': { target: backend, changeOrigin: true },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'react-vendor': ['react', 'react-dom', 'react-router-dom'],
          'charts-vendor': ['recharts'],
          'query-vendor': ['@tanstack/react-query', 'axios'],
          'ui-vendor': ['react-hot-toast'],
        },
      },
    },
  },
})
