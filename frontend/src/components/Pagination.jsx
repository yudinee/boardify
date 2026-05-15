import styles from './Pagination.module.css'

export default function Pagination({ currentPage, totalPages, onPageChange }) {
  if (totalPages <= 1) return null

  return (
    <div className={styles.pagination}>
      <button disabled={currentPage === 0} onClick={() => onPageChange(currentPage - 1)}>
        &lt;
      </button>
      {Array.from({ length: totalPages }, (_, i) => (
        <button
          key={i}
          className={i === currentPage ? styles.active : ''}
          onClick={() => onPageChange(i)}
        >
          {i + 1}
        </button>
      ))}
      <button disabled={currentPage === totalPages - 1} onClick={() => onPageChange(currentPage + 1)}>
        &gt;
      </button>
    </div>
  )
}
