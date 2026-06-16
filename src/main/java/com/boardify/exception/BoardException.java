package com.boardify.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BoardException extends RuntimeException {

  private final HttpStatus status;

  public BoardException(String message, HttpStatus status) {
    super(message);
    this.status = status;
  }
}