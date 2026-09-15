import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { fetchProfile, loginUser, registerUser, updateProfile, uploadAvatar } from '../api/user'
import { clearUserAuth, getUserAuth, saveUserAuth } from '../auth/userAuth'

export const useAuthStore = defineStore('user-auth', () => {
  const session = ref(getUserAuth())
  const profile = ref(session.value?.profile || null)
  const loading = ref(false)
  const isAuthenticated = computed(() => Boolean(session.value?.token))

  function persist(nextSession, nextProfile = profile.value) {
    session.value = nextSession
    profile.value = nextProfile
    saveUserAuth({ ...nextSession, profile: nextProfile })
  }

  async function signIn(payload) {
    loading.value = true
    try {
      const auth = await loginUser(payload)
      persist(auth, null)
      await refreshProfile()
    } finally { loading.value = false }
  }

  async function signUp(payload) {
    loading.value = true
    try {
      const auth = await registerUser(payload)
      persist(auth, null)
      await refreshProfile()
    } finally { loading.value = false }
  }

  async function refreshProfile() {
    if (!session.value?.token) return null
    const nextProfile = await fetchProfile()
    persist({ ...session.value, nickname: nextProfile.nickname }, nextProfile)
    return nextProfile
  }

  async function saveProfile(payload) {
    const nextProfile = await updateProfile(payload)
    persist({ ...session.value, nickname: nextProfile.nickname }, nextProfile)
    return nextProfile
  }

  async function saveAvatar(file, onProgress) {
    const nextProfile = await uploadAvatar(file, onProgress)
    persist({ ...session.value, nickname: nextProfile.nickname }, nextProfile)
    return nextProfile
  }

  function logout() {
    clearUserAuth()
    session.value = null
    profile.value = null
  }

  if (typeof window !== 'undefined') {
    window.addEventListener('musicdemo1:user-session-expired', logout)
  }

  return { session, profile, loading, isAuthenticated, signIn, signUp, refreshProfile, saveProfile, saveAvatar, logout }
})
