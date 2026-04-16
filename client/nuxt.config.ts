import tailwindcss from '@tailwindcss/vite'

export default defineNuxtConfig({
  compatibilityDate: '2026-04-06',
  devtools: { enabled: true },
  css: ['~/assets/css/main.css'],
  modules: ['@nuxt/test-utils/module', 'shadcn-nuxt'],
  shadcn: {
    prefix: 'Ui',
    componentDir: './components/ui',
  },
  experimental: {
    appManifest: false,
  },
  vite: {
    plugins: [tailwindcss()],
    optimizeDeps: {
      include: ['clsx', 'tailwind-merge', '@heroicons/vue/20/solid'],
    },
  },
  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080/api',
    },
  },
})
