package com.boardify.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  // 예상치 못한 모든 예외 처리 (500 Internal Server Error)
  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleException(Exception e) {
    e.printStackTrace();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류가 발생했습니다.");
  }

  //게시글 관련 예외 처리
  @ExceptionHandler(BoardException.class)
  public ResponseEntity<String> handleBoardException(BoardException e) {
    return ResponseEntity.status(e.getStatus()).body(e.getMessage());
  }

  //회원 관련 예외 처리
  @ExceptionHandler(MemberException.class)
  public ResponseEntity<String> handleMemberException(MemberException e) {
    return ResponseEntity.status(e.getStatus()).body(e.getMessage());
  }
}
