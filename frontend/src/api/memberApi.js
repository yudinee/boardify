import api from './axiosInstance'

// 회원가입
export const signup = ({ email, password, nickname }) =>
    api.post('/auth/signup', { email, password, nickname });

// 로그인
export const login = ({ email, password }) =>
    api.post('/auth/login', { email, password });

//토큰 재발급
export const refresh = (refreshToken) =>
    api.post('/auth/refresh', { refreshToken });

//로그아웃
export const logout = () =>
    api.post('/auth/logout');