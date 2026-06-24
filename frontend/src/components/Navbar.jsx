import { Link, useNavigate } from 'react-router-dom'
import styles from './Navbar.module.css'
import { logout } from '../api/memberApi'

export default function Navbar() {
  const navigate = useNavigate()
  const token = localStorage.getItem('accessToken')

  async function handleLogout() {
    try {
      await logout()  // 백엔드 Redis에서 refreshToken 삭제
    } catch {
      // 실패해도 로컬은 지움
    } finally {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('userEmail')
      navigate('/login')
    }
  }
  return (
    <nav className={styles.navbar}>
      <Link to="/boards" className={styles.logo}>Boardify</Link>
      <div className={styles.links}>
        {token ? (
          <>
            <Link to="/mypage">마이페이지</Link>
            <button onClick={handleLogout}>로그아웃</button>
          </>
        ) : (
          <>
            <Link to="/login">로그인</Link>
            <Link to="/register">회원가입</Link>
          </>
        )}
      </div>
    </nav>
  )
}
