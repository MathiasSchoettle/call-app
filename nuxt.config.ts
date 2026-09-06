export default defineNuxtConfig({
  ssr: false,
  nitro: {
    preset: 'static',
  },
  app: {
    head: {
      title: 'VoIP Prototype',
      meta: [
        { name: 'viewport', content: 'width=device-width, initial-scale=1, viewport-fit=cover' },
      ],
    },
  },
})
