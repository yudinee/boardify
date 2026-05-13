package com.boardify.domain.notification.entity;

import com.boardify.domain.board.entity.Board;
import com.boardify.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "board_id", nullable = false)
  private Board board;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private NotificationType type;

  @Column(nullable = false)
  private boolean isRead;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Builder
  public Notification(Board board, Member member, NotificationType type){
    this.board = board;
    this.member = member;
    this.isRead = false;
    this.createdAt = LocalDateTime.now();
  }

  public void markAsRead(){
    this.isRead = true;
  }

  public enum NotificationType{
    COMMENT, BOOKMARK
  }

}
