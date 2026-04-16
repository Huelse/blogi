export default defineNuxtPlugin(async () => {
  const auth = useAuth()

  await callOnce('auth-session', async () => {
    await auth.restoreSession()
  })
})
