import type {
  VisitorProfile,
  VisitorProfilePayload,
  VisitorProfileRequest,
  VisitorResolvePayload,
} from '~/types/blogi'

const fingerprintStorageKey = 'blogi.visitor.fingerprintHash'
const profileStorageKey = 'blogi.visitor.profile'
const fingerprintPattern = /^[a-f0-9]{64}$/

export function useVisitorIdentity() {
  const api = useApiClient()
  const fingerprintHash = useState('visitor-fingerprint-hash', () => '')
  const profile = useState<VisitorProfile | null>('visitor-profile', () => null)

  async function ensureFingerprintHash() {
    if (!import.meta.client) {
      return ''
    }

    if (fingerprintPattern.test(fingerprintHash.value)) {
      return fingerprintHash.value
    }

    const cached = window.localStorage.getItem(fingerprintStorageKey)
    if (cached && fingerprintPattern.test(cached)) {
      fingerprintHash.value = cached
      return cached
    }

    const nextFingerprintHash = await createBrowserFingerprintHash()
    fingerprintHash.value = nextFingerprintHash
    window.localStorage.setItem(fingerprintStorageKey, nextFingerprintHash)
    return nextFingerprintHash
  }

  async function loadProfile(force = false) {
    if (!import.meta.client) {
      return null
    }

    const currentFingerprintHash = await ensureFingerprintHash()
    if (!force && profile.value?.fingerprintHash === currentFingerprintHash) {
      return profile.value
    }

    if (!force) {
      const cachedProfile = readCachedProfile(currentFingerprintHash)
      if (cachedProfile) {
        profile.value = cachedProfile
        return cachedProfile
      }
    }

    const resolvedProfile = await api<VisitorProfile | null>('/visitors/resolve', {
      method: 'POST',
      body: { fingerprintHash: currentFingerprintHash } satisfies VisitorResolvePayload,
    })
    profile.value = resolvedProfile
    writeCachedProfile(resolvedProfile)
    return resolvedProfile
  }

  async function saveProfile(payload: VisitorProfilePayload) {
    const currentFingerprintHash = await ensureFingerprintHash()
    const savedProfile = await api<VisitorProfile>('/visitors/profile', {
      method: 'PUT',
      body: {
        fingerprintHash: currentFingerprintHash,
        displayName: payload.displayName,
        email: payload.email,
      } satisfies VisitorProfileRequest,
    })
    profile.value = savedProfile
    writeCachedProfile(savedProfile)
    return savedProfile
  }

  return {
    fingerprintHash,
    profile,
    ensureFingerprintHash,
    loadProfile,
    saveProfile,
  }
}

async function createBrowserFingerprintHash() {
  const fingerprintPayload = [
    navigator.userAgent,
    navigator.language,
    navigator.languages?.join(',') ?? '',
    navigator.platform,
    String(navigator.hardwareConcurrency ?? ''),
    String((navigator as Navigator & { deviceMemory?: number }).deviceMemory ?? ''),
    String(navigator.maxTouchPoints ?? ''),
    Intl.DateTimeFormat().resolvedOptions().timeZone,
    `${screen.width}x${screen.height}x${screen.colorDepth}x${screen.pixelDepth}`,
    collectCanvasFingerprint(),
    collectWebGlFingerprint(),
  ].join('|')

  return hashText(fingerprintPayload)
}

async function hashText(value: string) {
  if (window.crypto?.subtle) {
    const bytes = new TextEncoder().encode(value)
    const digest = await window.crypto.subtle.digest('SHA-256', bytes)
    return Array.from(new Uint8Array(digest))
      .map((byte) => byte.toString(16).padStart(2, '0'))
      .join('')
  }

  return legacyHexHash(value)
}

function legacyHexHash(value: string) {
  const seeds = [
    0x811c9dc5, 0x45d9f3b, 0x27d4eb2d, 0x165667b1, 0x9e3779b9, 0x85ebca6b, 0xc2b2ae35, 0x7f4a7c15,
  ]
  const hashes = seeds.map((seed) => {
    let hash = seed
    for (let index = 0; index < value.length; index += 1) {
      hash ^= value.charCodeAt(index)
      hash = Math.imul(hash, 16777619)
    }
    return (hash >>> 0).toString(16).padStart(8, '0')
  })

  return hashes.join('')
}

function collectCanvasFingerprint() {
  try {
    const canvas = document.createElement('canvas')
    canvas.width = 280
    canvas.height = 80
    const context = canvas.getContext('2d')
    if (!context) {
      return ''
    }

    context.textBaseline = 'top'
    context.font = '16px Arial'
    context.fillStyle = '#f59e0b'
    context.fillRect(0, 0, 120, 34)
    context.fillStyle = '#111827'
    context.fillText('Blogi visitor fingerprint', 8, 12)
    context.fillStyle = 'rgba(37, 99, 235, 0.72)'
    context.fillText('browser visitor', 12, 42)
    return canvas.toDataURL()
  } catch {
    return ''
  }
}

function collectWebGlFingerprint() {
  try {
    const canvas = document.createElement('canvas')
    const gl = (canvas.getContext('webgl') ??
      canvas.getContext('experimental-webgl')) as WebGLRenderingContext | null
    if (!gl || !('getExtension' in gl)) {
      return ''
    }

    const debugInfo = gl.getExtension('WEBGL_debug_renderer_info')
    if (!debugInfo) {
      return ''
    }

    return [
      gl.getParameter(debugInfo.UNMASKED_VENDOR_WEBGL),
      gl.getParameter(debugInfo.UNMASKED_RENDERER_WEBGL),
    ].join('|')
  } catch {
    return ''
  }
}

function readCachedProfile(currentFingerprintHash: string) {
  try {
    const rawProfile = window.localStorage.getItem(profileStorageKey)
    if (!rawProfile) {
      return null
    }

    const cachedProfile = JSON.parse(rawProfile) as VisitorProfile
    return cachedProfile?.fingerprintHash === currentFingerprintHash ? cachedProfile : null
  } catch {
    return null
  }
}

function writeCachedProfile(profile: VisitorProfile | null) {
  if (!profile) {
    window.localStorage.removeItem(profileStorageKey)
    return
  }

  window.localStorage.setItem(profileStorageKey, JSON.stringify(profile))
}
