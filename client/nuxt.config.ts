import tailwindcss from '@tailwindcss/vite'

export default defineNuxtConfig({
  compatibilityDate: '2026-04-06',
  devtools: { enabled: true },
  css: ['~/assets/css/main.css'],
  modules: ['@nuxt/test-utils/module', '@nuxtjs/i18n', 'shadcn-nuxt'],
  shadcn: {
    prefix: 'Ui',
    componentDir: './components/ui',
  },
  i18n: {
    defaultLocale: 'zh',
    detectBrowserLanguage: {
      alwaysRedirect: false,
      cookieKey: 'blogi_locale',
      redirectOn: 'root',
      useCookie: true,
    },
    langDir: 'locales',
    locales: [
      {
        code: 'zh',
        file: 'zh.json',
        language: 'zh-CN',
        name: '简体中文',
      },
      {
        code: 'en',
        file: 'en.json',
        language: 'en-US',
        name: 'English',
      },
    ],
    strategy: 'no_prefix',
    vueI18n: './i18n.config.ts',
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
