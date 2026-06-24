import axios from 'axios'
import { refresh } from './memberApi'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

// 요청 인터셉터 - accessToken 키 이름 통일
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 응답 인터셉터 - 401 뜨면 리프레시 토큰으로 재발급 시도
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    // 401이고 재시도 안 한 요청이면
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true // 무한루프 방지

      try {
        const refreshToken = localStorage.getItem('refreshToken')
        const { data } = await refresh(refreshToken)

        // 새 accessToken 저장 후 원래 요청 재시도
        localStorage.setItem('accessToken', data.accessToken)
        originalRequest.headers.Authorization = `Bearer ${data.accessToken}`
        return api(originalRequest)

      } catch (e) {
        // 리프레시도 실패하면 로그아웃
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        window.location.href = '/login'
      }
    }

    return Promise.reject(error)
  }
)

export default api