package com.boardify.domain.boardimage.entity;

import com.boardify.domain.board.entity.Board;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "board_image")
public class BoardImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "board_id", nullable = false)
  private Board board;

  @Column(nullable = false)
  private String imageUrl;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public BoardImage(Board board, String imageUrl) {
    this.board = board;
    this.imageUrl = imageUrl;
    this.createdAt = LocalDateTime.now();
  }


}
