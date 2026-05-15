import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Viewer } from '@toast-ui/react-editor'
import '@toast-ui/editor/dist/toastui-editor.css'
import api from '../api/axiosInstance'
import styles from './BoardDetailPage.module.css'

export default function BoardDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const token = localStorage.getItem('token')

  const [board, setBoard] = useState(null)
  const [liked, setLiked] = useState(false)
  const [comments, setComments] = useState([])
  const [commentInput, setCommentInput] = useState('')
  const [editingId, setEditingId] = useState(null)
  const [editContent, setEditContent] = useState('')

  useEffect(() => {
    fetchBoard()
    fetchComments()
  }, [id])

  async function fetchBoard() {
    try {
      const { data } = await api.get(`/boards/${id}`)
      setBoard(data)
      setLiked(data.likedByCurrentUser ?? false)
    } catch {
      navigate('/')
    }
  }

  async function fetchComments() {
    try {
      const { data } = await api.get(`/boards/${id}/comments`)
      setComments(Array.isArray(data) ? data : data.content ?? [])
    } catch {
      setComments([])
    }
  }

  async function handleLike() {
    if (!token) { navigate('/login'); return }
    try {
      const { data } = await api.post(`/boards/${id}/like`)
      setBoard((prev) => ({ ...prev, likeCount: data.likeCount }))
      setLiked(data.liked)
    } catch {}
  }

  async function handleDelete() {
    if (!confirm('정말 삭제하시겠습니까?')) return
    try {
      await api.delete(`/boards/${id}`)
      navigate('/')
    } catch {
      alert('삭제에 실패했습니다.')
    }
  }

  async function handleCommentSubmit(e) {
    e.preventDefault()
    if (!token) { navigate('/login'); return }
    if (!commentInput.trim()) return
    try {
      await api.post(`/boards/${id}/comments`, { content: commentInput })
      setCommentInput('')
      fetchComments()
    } catch {}
  }

  async function handleCommentEdit(commentId) {
    if (!editContent.trim()) return
    try {
      await api.put(`/comments/${commentId}`, { content: editContent })
      setEditingId(null)
      fetchComments()
    } catch {}
  }

  async function handleCommentDelete(commentId) {
    if (!confirm('댓글을 삭제하시겠습니까?')) return
    try {
      await api.delete(`/comments/${commentId}`)
      fetchComments()
    } catch {}
  }

  if (!board) return <p className={styles.loading}>불러오는 중...</p>

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <h1 className={styles.title}>{board.title}</h1>
        <div className={styles.meta}>
          <span>{board.authorNickname}</span>
          <span>{new Date(board.createdAt).toLocaleString('ko-KR')}</span>
          <button className={liked ? styles.likedBtn : styles.likeBtn} onClick={handleLike}>
            ♥ {board.likeCount}
          </button>
        </div>
        <div className={styles.content}>
          <Viewer initialValue={board.content} />
        </div>
        {token && (
          <div className={styles.actions}>
            <button onClick={() => navigate(`/boards/${id}/edit`)}>수정</button>
            <button className={styles.deleteBtn} onClick={handleDelete}>삭제</button>
          </div>
        )}
      </div>

      <div className={styles.commentSection}>
        <h3>댓글 ({comments.length})</h3>
        {token && (
          <form className={styles.commentForm} onSubmit={handleCommentSubmit}>
            <input
              placeholder="댓글을 입력하세요"
              value={commentInput}
              onChange={(e) => setCommentInput(e.target.value)}
            />
            <button type="submit">등록</button>
          </form>
        )}
        <ul className={styles.commentList}>
          {comments.map((comment) => (
            <li key={comment.id} className={styles.comment}>
              {editingId === comment.id ? (
                <div className={styles.editForm}>
                  <input
                    value={editContent}
                    onChange={(e) => setEditContent(e.target.value)}
                  />
                  <button onClick={() => handleCommentEdit(comment.id)}>저장</button>
                  <button onClick={() => setEditingId(null)}>취소</button>
                </div>
              ) : (
                <>
                  <div className={styles.commentMeta}>
                    <strong>{comment.authorNickname}</strong>
                    <span>{new Date(comment.createdAt).toLocaleString('ko-KR')}</span>
                  </div>
                  <p className={styles.commentContent}>{comment.content}</p>
                  {token && (
                    <div className={styles.commentActions}>
                      <button
                        onClick={() => {
                          setEditingId(comment.id)
                          setEditContent(comment.content)
                        }}
                      >
                        수정
                      </button>
                      <button onClick={() => handleCommentDelete(comment.id)}>삭제</button>
                    </div>
                  )}
                </>
              )}
            </li>
          ))}
        </ul>
      </div>
    </div>
  )
}
