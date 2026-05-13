package com.boardify.domain.bookmark.entity;

import com.boardify.domain.board.entity.Board;
import com.boardify.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "bookmark",
uniqueConstraints = @UniqueConstraint(columnNames = {"board_id", "member_id"}))
public class Bookmark {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private  Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "board_id", nullable = false)
  private Board board;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public Bookmark(Board board, Member member) {
    this.board = board;
    this.member = member;
    this.createdAt = LocalDateTime.now();
  }

}
