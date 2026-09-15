const KEY = 'musicdemo1.admin.auth'

export function getAdminAuth() {
  try {
    const raw = localStorage.getItem(KEY)
    if (!raw) return null
    const auth = JSON.parse(raw)
    return auth.role === 'ADMIN' && auth.token ? auth : null
  } catch {
    return null
  }
}

export function saveAdminAuth(auth) {
  try { localStorage.setItem(KEY, JSON.stringify(auth)) } catch { /* ignore storage failures */ }
}

export function clearAdminAuth() {
  try { localStorage.removeItem(KEY) } catch { /* ignore storage failures */ }
}
