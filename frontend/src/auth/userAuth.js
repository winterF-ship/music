const KEY = 'musicdemo1.user.auth'

export function getUserAuth() {
  try {
    const raw = localStorage.getItem(KEY)
    if (!raw) return null
    const auth = JSON.parse(raw)
    return auth.role === 'USER' && auth.token ? auth : null
  } catch {
    return null
  }
}

export function saveUserAuth(auth) {
  try { localStorage.setItem(KEY, JSON.stringify(auth)) } catch { /* private mode can reject storage */ }
}

export function clearUserAuth() {
  try { localStorage.removeItem(KEY) } catch { /* private mode can reject storage */ }
}
