package com.boardify.exception;

public class BoardException extends RuntimeException {

  public BoardException(String message) {
        super(message);
  }

  public BoardException(Throwable cause) {
    super(cause);
  }

  public BoardException(String message, Throwable cause) {
    super(message, cause);
  }

}
