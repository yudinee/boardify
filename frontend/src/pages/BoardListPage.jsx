import { useState, useEffect, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import Pagination from '../components/Pagination'
import styles from './BoardListPage.module.css'
import { getList } from '../api/boardapi'

export default function BoardListPage() {
  const navigate = useNavigate()
  const [boards, setBoards] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(false)

  const fetchBoards = useCallback(async () => {
    setLoading(true)
    try {
      const { data } = await getList({ page, size: 10, sort: 'createdAt,desc' })
      setBoards(data.content ?? [])
      setTotalPages(data.totalPages ?? 0)
    } catch {
      setBoards([])
    } finally {
      setLoading(false)
    }
  }, [page])

  useEffect(() => {
    fetchBoards()
  }, [fetchBoards])

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.heading}>게시판</h2>
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
                <td>{board.author}</td>
                <td>{board.createdAt.replace('T', ' ').slice(0, 10)}</td>
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