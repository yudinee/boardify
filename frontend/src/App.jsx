import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Navbar from './components/Navbar'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import BoardListPage from './pages/BoardListPage'
import BoardDetailPage from './pages/BoardDetailPage'
import BoardFormPage from './pages/BoardFormPage'

function PrivateRoute({ children }) {
  const token = localStorage.getItem('accessToken')
  if (!token) {
    alert('로그인이 필요한 서비스 입니다.')
    return <Navigate to="/login" replace />
  }
  return children
}

export default function App() {
  return (
    <BrowserRouter>
      <Navbar />
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/boards" element={<PrivateRoute><BoardListPage /></PrivateRoute>} />
        <Route path="/boards/new" element={<PrivateRoute><BoardFormPage /></PrivateRoute>} />
        <Route path="/boards/:id/edit" element={<PrivateRoute><BoardFormPage /></PrivateRoute>} />
        <Route path="/boards/:id" element={<PrivateRoute><BoardDetailPage /></PrivateRoute>} />
      </Routes>
    </BrowserRouter>
  )
}
