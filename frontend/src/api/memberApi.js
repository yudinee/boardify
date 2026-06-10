import api from './axiosInstance'

// 회원가입
export const signup = ({ email, password, nickname }) =>
    api.post('/auth/signup', { email, password, nickname });

// 로그인
export const login = ({ email, password }) =>
    api.post('/auth/login', { email, password });