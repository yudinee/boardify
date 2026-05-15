import { useState, useEffect, useRef } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Editor } from '@toast-ui/react-editor'
import '@toast-ui/editor/dist/toastui-editor.css'
import api from '../api/axiosInstance'
import styles from './BoardFormPage.module.css'

export default function BoardFormPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(id)
  const editorRef = useRef(null)
  const [title, setTitle] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    if (isEdit) {
      api.get(`/boards/${id}`)
        .then(({ data }) => {
          setTitle(data.title)
          editorRef.current?.getInstance().setHTML(data.content)
        })
        .catch(() => navigate('/'))
    }
  }, [id])

  async function uploadImage(blob, callback) {
    const formData = new FormData()
    formData.append('image', blob)
    try {
      const { data } = await api.post('/upload', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      })
      callback(data.url, blob.name)
    } catch {
      callback('', '이미지 업로드 실패')
    }
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    const content = editorRef.current?.getInstance().getHTML() ?? ''
    if (!title.trim() || content === '<p><br></p>' || !content.trim()) {
      setError('제목과 내용을 모두 입력하세요.')
      return
    }
    try {
      if (isEdit) {
        await api.put(`/boards/${id}`, { title, content })
        navigate(`/boards/${id}`)
      } else {
        const { data } = await api.post('/boards', { title, content })
        navigate(`/boards/${data.id}`)
      }
    } catch (err) {
      setError(err.response?.data?.message || '저장에 실패했습니다.')
    }
  }

  return (
    <div className={styles.container}>
      <form className={styles.form} onSubmit={handleSubmit}>
        <h2>{isEdit ? '게시글 수정' : '게시글 작성'}</h2>
        {error && <p className={styles.error}>{error}</p>}
        <input
          placeholder="제목을 입력하세요"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          required
        />
        <div className={styles.editorWrap}>
          <Editor
            ref={editorRef}
            initialEditType="wysiwyg"
            previewStyle="vertical"
            height="420px"
            placeholder="내용을 입력하세요"
            hooks={{ addImageBlobHook: uploadImage }}
          />
        </div>
        <div className={styles.buttons}>
          <button type="button" onClick={() => navigate(-1)}>취소</button>
          <button type="submit">{isEdit ? '수정 완료' : '작성 완료'}</button>
        </div>
      </form>
    </div>
  )
}
