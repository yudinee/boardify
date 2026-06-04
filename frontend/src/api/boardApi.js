import api from './axiosInstance'

//글쓰기
export const createBoard = ({title, content}) => 
    api.post('/boards', {title,content})


//글목록
export const getList = (params) => api.get('/boards',{params})

//글상세
export const getOne = (id) => api.get(`/boards/${id}`)

//글수정
export const updateBoard = (id, {title, content}) => api.put(`/boards/${id}`, {title, content})

//글삭제
export const deleteBoard = (id) => api.delete(`/boards/${id}`)