package com.boardify.domain.member.service;

import com.boardify.domain.member.dto.LoginRequest;
import com.boardify.domain.member.dto.LoginResponse;
import com.boardify.domain.member.dto.SignupRequest;
import com.boardify.domain.member.entity.Member;
import com.boardify.domain.member.repository.MemberRepository;
import com.boardify.exception.MemberException;
import com.boardify.jwt.JwtProvider;
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

  public LoginResponse login(LoginRequest request) {

   Member member = memberRepository.findByEmail(request.getEmail())
       .orElseThrow(()-> new MemberException("존재하지 않는 이메일입니다.", HttpStatus.NOT_FOUND));

   if(!passwordEncoder.matches(request.getPassword(), member.getPassword())){
     throw new MemberException("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
   }

   String token = jwtProvider.generateToken(member.getEmail());

   return new  LoginResponse(token);

  }

}
