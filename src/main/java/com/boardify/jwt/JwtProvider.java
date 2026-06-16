package com.boardify.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtProvider {

  @Value("${jwt.secret}")
  private String secretKey;

  @Value("${jwt.expiration}")
  private long expiration;

  @Value("${jwt.refresh-expiration}")
  private long refreshExpiration;

  /*
  * 토큰 생성
  * 로그인 성공 시 email을 담아서 토큰을 만들어 반환
  * email = 실제 로그인 한 사용자의 이메일
  * 나중에 요청이 들어오면 토큰에서 email을 꺼내서 누가 요청했는지 알 수 있음
  * */
  public String generateAccessToken(String email) {
    return Jwts.builder()
        .setSubject(email)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  public String generateRefreshToken(String email) {
    return Jwts.builder()
        .setSubject(email)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
        .signWith(getSigningKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  /*
  * 토큰에서 이메일 추출
  * setSubject(email)로 넣었으니 getSubject()로 꺼냄
  * JwtFilter에서 "이 토큰 주인이 누구야? 할 때 사용
  * */
  public String getEmail(String token){
    return getClaims(token).getSubject();
  }

  /*
  * 토큰 유효성 검증
  * getClaims()가 예외 없이 통과되면 정상 토큰 -> true 반환
  * jjwt가 내부적으로 형식, 서명, 만료시간을 다 검사함
  * 문제가 있으면 예외를 던짐
  * */
  public boolean validateToken(String token){
    try{
      getClaims(token);
      return true;
    }catch (ExpiredJwtException e){
      throw new JwtException("토큰이 만료되었습니다.");
    }catch (Exception e){
      throw new JwtException("유효하지 않은 토큰입니다.");
    }
  }

  /*
  * 토큰 파싱 (내부용)
  * 토큰 문자열을 해석해서 안에 든 데이트(claims)를 꺼냄
  *  Claims = 토큰 안에 저장된 정보 묶음(subject, issuedAt, expiration 등)
  * private인 이유 = getEmail(), validateToken() 내부에서만 쓰기 때문
  * */
  private Claims getClaims(String token){
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();

  }

  /*
  * 시크릿 키 변환(내부용)
  * application.properties의 문자열 시크릿 키를 jjwt가 쓸 수 있는 key 객체로 변환
  * 문자열 -> 바이트 배열 -> key 객체
  * */
  private Key getSigningKey() {
    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
    return Keys.hmacShaKeyFor(keyBytes);
  }


}
