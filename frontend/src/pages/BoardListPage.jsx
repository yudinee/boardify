import { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/axiosInstance'
import Pagination from '../components/Pagination'
import styles from './BoardListPage.module.css'

export default function BoardListPage() {
  const navigate = useNavigate()
  const [boards, setBoards] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [sort, setSort] = useState('latest')
  const [keyword, setKeyword] = useState('')
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(false)

  const fetchBoards = useCallback(async () => {
    setLoading(true)
    try {
      const { data } = await api.get('/boards', {
        params: { page, size: 10, sort, keyword: search },
      })
      setBoards(data.content ?? [])
      setTotalPages(data.totalPages ?? 0)
    } catch {
      setBoards([])
    } finally {
      setLoading(false)
    }
  }, [page, sort, search])

  useEffect(() => {
    fetchBoards()
  }, [fetchBoards])

  function handleSearch(e) {
    e.preventDefault()
    setPage(0)
    setSearch(keyword)
  }

  function handleSort(value) {
    setSort(value)
    setPage(0)
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.heading}>게시판</h2>
      </div>
      <div className={styles.toolbar}>
        <div className={styles.sortBtns}>
          <button
            className={sort === 'latest' ? styles.active : ''}
            onClick={() => handleSort('latest')}
          >
            최신순
          </button>
          <button
            className={sort === 'likes' ? styles.active : ''}
            onClick={() => handleSort('likes')}
          >
            찜 많은 순
          </button>
        </div>
        <form className={styles.searchForm} onSubmit={handleSearch}>
          <input
            placeholder="제목 검색"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
          <button type="submit">검색</button>
        </form>
      </div>

      {loading ? (
        <p className={styles.empty}>불러오는 중...</p>
      ) : boards.length === 0 ? (
        <p className={styles.empty}>게시글이 없습니다.</p>
      ) : (
        <table className={styles.table}>
          <thead>
            <tr>
              <th>번호</th>
              <th>제목</th>
              <th>작성자</th>
              <th>찜</th>
              <th>날짜</th>
            </tr>
          </thead>
          <tbody>
            {boards.map((board) => (
              <tr
                key={board.id}
                className={styles.row}
                onClick={() => navigate(`/boards/${board.id}`)}
              >
                <td>{board.id}</td>
                <td className={styles.title}>{board.title}</td>
                <td>{board.authorNickname}</td>
                <td>♥ {board.likeCount}</td>
                <td>{new Date(board.createdAt).toLocaleDateString('ko-KR')}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
      <button className={styles.writeBtn} onClick={() => navigate('/boards/new')}>
        글쓰기
      </button>
    </div>
  )
}
