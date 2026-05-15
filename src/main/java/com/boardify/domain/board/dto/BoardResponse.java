package com.boardify.domain.board.dto;

import com.boardify.domain.board.entity.Board;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoardResponse {

  private Long id;
  private String title;
  private String content;
  private String author;
  private LocalDateTime createdAt;

  public static BoardResponse from(Board board){
    return BoardResponse.builder()
        .id(board.getId())
        .title(board.getTitle())
        .content(board.getContent())
        .author(board.getMember().getNickname())
        .createdAt(board.getCreatedAt())
        .build();
  }

}
