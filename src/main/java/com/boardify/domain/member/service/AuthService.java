package com.boardify.domain.member.service;

import com.boardify.domain.member.dto.LoginRequest;
import com.boardify.domain.member.dto.LoginResponse;
import com.boardify.domain.member.dto.SignupRequest;
import com.boardify.domain.member.dto.TokenRefreshRequest;
import com.boardify.domain.member.entity.Member;
import com.boardify.domain.member.repository.MemberRepository;
import com.boardify.exception.MemberException;
import com.boardify.jwt.JwtProvider;
import com.boardify.redis.RedisService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProvider jwtProvider;
  private final RedisService redisService;

  /*
  * 회원가입
  * */
  @Transactional
  public void signup(SignupRequest request) {

    if(memberRepository.existsByEmail(request.getEmail())) {
      throw new MemberException("이미 사용중인 이메일입니다.", HttpStatus.CONFLICT);
    }

    Member member = Member.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .nickname(request.getNickname())
        .build();

    memberRepository.save(member);

  }


  /*
  * 로그인
  * */
  public LoginResponse login(LoginRequest request) {

   Member member = memberRepository.findByEmail(request.getEmail())
       .orElseThrow(()-> new MemberException("존재하지 않는 이메일입니다.", HttpStatus.NOT_FOUND));

   if(!passwordEncoder.matches(request.getPassword(), member.getPassword())){
     throw new MemberException("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
   }

   String accessToken = jwtProvider.generateAccessToken(member.getEmail());
   String refreshToken = jwtProvider.generateRefreshToken(member.getEmail());

   redisService.save("refresh:" + member.getEmail(), refreshToken, 7, TimeUnit.DAYS);

   return new  LoginResponse(accessToken, refreshToken, member.getEmail());

  }


  /*
  * accesstoken 재발급
  * */
  public LoginResponse refresh(TokenRefreshRequest request){

    String refreshToken = request.getRefreshToken();

    // 1. refresh token에서 이메일 추출
    String email = jwtProvider.getEmail(refreshToken);

    // 2. Redis에서 저장된 refresh token 조회
    String savedToken = redisService.get("refresh:" + email);

    // 3. Redis에 없거나 요청한 토큰이랑 다르면 예외처리
    if(savedToken == null || !savedToken.equals(refreshToken)) {
      throw new MemberException("유효하지 않은 refresh token입니다.", HttpStatus.UNAUTHORIZED);
    }

    // 4. 새 access token 발급
    String newAccessToken = jwtProvider.generateAccessToken(email);

    return new LoginResponse(newAccessToken, refreshToken, email);

  }


  /*
  * 로그아웃
  * */
  public void logout(String token) {
    redisService.delete("refresh:" + token);
  }












}
