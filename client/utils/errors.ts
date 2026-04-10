export function getErrorMessage(error: unknown) {
  const message = (error as { data?: { message?: string } })?.data?.message
  if (typeof message === 'string' && message.length > 0) {
    return message
  }

  return '请求失败，请稍后重试'
}
