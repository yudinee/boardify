package com.boardify.domain.member.controller;

import com.boardify.domain.member.dto.LoginRequest;
import com.boardify.domain.member.dto.LoginResponse;
import com.boardify.domain.member.dto.SignupRequest;
import com.boardify.domain.member.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  /*
  * 회원가입
  * */
  @PostMapping("/signup")
  public ResponseEntity<Void> signup(@RequestBody @Valid SignupRequest request){
    authService.signup(request);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  /*
  * 로그인
  * */
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request){
    return ResponseEntity.ok(authService.login(request));
  }
}
