package com.boardify.domain.board.repository;

import com.boardify.domain.board.entity.Board;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {

  Page<Board> findAllByDeletedFalse(Pageable pageable);
  Optional<Board> findByIdAndDeletedFalse(Long id);

}
