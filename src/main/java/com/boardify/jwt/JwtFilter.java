package com.boardify.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

  private final JwtProvider jwtProvider;
  private final UserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    // 1. 요청 헤더에서 토큰 꺼내기
    String token = resolveToken(request);

    // 2. 토큰이 있으면 유효성 검사 시도
    if (token != null) {
      try {
        // 3. 토큰 유효성 검사 (만료/변조 여부 확인)
        if (jwtProvider.validateToken(token)) {
          // 4. 토큰에서 이메일 꺼내기
          String email = jwtProvider.getEmail(token);

          // 5. 이메일로 유저 정보 가져오기
          UserDetails userDetails = userDetailsService.loadUserByUsername(email);

          // 6. 인증 객체 만들어서 SecurityContext에 저장
          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      } catch (Exception e) {
        // [추가] 토큰이 만료되거나 유효하지 않아도 예외를 던지지 않고 무시
        // → 인증 없이 다음 필터로 넘어감
        // → permitAll() 엔드포인트는 그냥 통과, authenticated() 엔드포인트는 Security가 401 반환
      }
    }

    // 7. 다음 필터로 넘기기 (인증 성공 여부와 관계없이 항상 실행)
    filterChain.doFilter(request, response);
  }

  // Authorization 헤더에서 토큰 추출
  // "Bearer eyJhbGci..." → "eyJhbGci..." 앞의 "Bearer " 제거
  private String resolveToken(HttpServletRequest request) {
    String bearer = request.getHeader("Authorization");
    if (bearer != null && bearer.startsWith("Bearer ")) {
      return bearer.substring(7);
    }
    return null;
  }
}