const TOKEN_KEY = 'distribution-platform-token'
const USER_KEY = 'distribution-platform-user'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function getCurrentUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw)
  } catch {
    clearAuthSession()
    return null
  }
}

export function isAuthenticated() {
  return Boolean(getToken())
}

export function saveAuthSession(loginResponse) {
  localStorage.setItem(TOKEN_KEY, loginResponse.token)
  localStorage.setItem(USER_KEY, JSON.stringify(loginResponse.user))
  window.dispatchEvent(new Event('auth-session-change'))
}

export function clearAuthSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  window.dispatchEvent(new Event('auth-session-change'))
}
