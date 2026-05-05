import authMiddleware from '~/middleware/auth'
import guestMiddleware from '~/middleware/guest'

export default defineNuxtRouteMiddleware((to, from) => {
  return authMiddleware(to, from) || guestMiddleware(to, from)
})
