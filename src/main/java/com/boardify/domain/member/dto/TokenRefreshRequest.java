package com.boardify.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TokenRefreshRequest {

  @NotBlank
  private String refreshToken;

}
