import api from './axiosInstance'

//글쓰기
export const createBoard = ({title, content}) => 
    api.post('/boards', {title,content})


//글목록
export const getList = (params) => api.get('/boards',{params})