import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.boardify.domain.member.dto.LoginRequest;
import com.boardify.domain.member.dto.LoginResponse;
import com.boardify.domain.member.dto.SignupRequest;
import com.boardify.domain.member.dto.TokenRefreshRequest;
import com.boardify.domain.member.entity.Member;
import com.boardify.domain.member.repository.MemberRepository;
import com.boardify.domain.member.service.AuthService;
import com.boardify.exception.MemberException;
import com.boardify.jwt.JwtProvider;
import com.boardify.redis.RedisService;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

  @InjectMocks
  private AuthService authService;

  @Mock
  private JwtProvider jwtProvider;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private RedisService redisService;  // 추가


  // ============================================================
  // 회원가입
  // ============================================================

  @Test
  @DisplayName("회원가입 성공")
  void signup_success() {
    // given
    SignupRequest request = new SignupRequest("test@test.com", "유진", "1234");

    given(memberRepository.existsByEmail(request.getEmail())).willReturn(false);
    given(passwordEncoder.encode(request.getPassword())).willReturn("암호화된 비밀번호");
    given(memberRepository.save(any(Member.class))).willReturn(null);

    // when
    authService.signup(request);

    // then
    then(memberRepository).should().existsByEmail(request.getEmail());
    then(passwordEncoder).should().encode(request.getPassword());
    then(memberRepository).should().save(any(Member.class));
  }

  @Test
  @DisplayName("회원가입 실패 - 이메일 중복")
  void signup_fail_duplicate_email() {
    // given
    SignupRequest request = new SignupRequest("test@test.com", "1234", "유진");

    given(memberRepository.existsByEmail(request.getEmail())).willReturn(true);

    // when & then
    assertThatThrownBy(() -> authService.signup(request))
        .isInstanceOf(MemberException.class)
        .hasMessage("이미 사용중인 이메일입니다.");
  }


  // ============================================================
  // 로그인
  // ============================================================

  @Test
  @DisplayName("로그인 성공")
  void login_success() {
    // given
    LoginRequest request = new LoginRequest("test@test.com", "1234");

    Member member = Member.builder()
        .email(request.getEmail())
        .password(request.getPassword())
        .build();

    given(memberRepository.findByEmail(request.getEmail())).willReturn(Optional.of(member));
    given(passwordEncoder.matches(request.getPassword(), member.getPassword())).willReturn(true);
    given(jwtProvider.generateAccessToken(member.getEmail())).willReturn("access-token");
    given(jwtProvider.generateRefreshToken(member.getEmail())).willReturn("refresh-token"); // 추가

    // when
    LoginResponse response = authService.login(request);

    // then
    assertThat(response.getAccessToken()).isEqualTo("access-token");
    assertThat(response.getRefreshToken()).isEqualTo("refresh-token"); // 추가

    then(memberRepository).should().findByEmail(request.getEmail());
    then(passwordEncoder).should().matches(request.getPassword(), member.getPassword());
    then(jwtProvider).should().generateAccessToken(member.getEmail());
    then(jwtProvider).should().generateRefreshToken(member.getEmail()); // 추가
    then(redisService).should().save(                                   // 추가
        eq("refresh:" + member.getEmail()),
        eq("refresh-token"),
        eq(7L),
        eq(TimeUnit.DAYS)
    );
  }

  @Test
  @DisplayName("로그인 실패 - 존재하지 않는 이메일")
  void login_fail_not_found_email() {
    // given
    LoginRequest request = new LoginRequest("없는이메일@test.com", "1234");

    given(memberRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(MemberException.class)
        .hasMessage("존재하지 않는 이메일입니다.");
  }

  @Test
  @DisplayName("로그인 실패 - 비밀번호 불일치")
  void login_fail_wrong_password() {
    // given
    LoginRequest request = new LoginRequest("test@test.com", "틀린비밀번호");

    Member member = Member.builder()
        .email("test@test.com")
        .password("암호화된비밀번호")
        .nickname("유진")
        .build();

    given(memberRepository.findByEmail(request.getEmail())).willReturn(Optional.of(member));
    given(passwordEncoder.matches(request.getPassword(), member.getPassword())).willReturn(false);

    // when & then
    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(MemberException.class)
        .hasMessage("비밀번호가 일치하지 않습니다.");
  }


  // ============================================================
  // 토큰 재발급
  // ============================================================

  @Test
  @DisplayName("토큰 재발급 성공")
  void refresh_success() {
    // given
    String refreshToken = "valid-refresh-token";
    String email = "test@test.com";
    TokenRefreshRequest request = new TokenRefreshRequest(refreshToken);

    given(jwtProvider.getEmail(refreshToken)).willReturn(email);
    given(redisService.get("refresh:" + email)).willReturn(refreshToken);
    given(jwtProvider.generateAccessToken(email)).willReturn("new-access-token");

    // when
    LoginResponse response = authService.refresh(request);

    // then
    assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
  }

  @Test
  @DisplayName("토큰 재발급 실패 - Redis에 토큰 없음")
  void refresh_fail_not_in_redis() {
    // given
    String refreshToken = "some-refresh-token";
    String email = "test@test.com";
    TokenRefreshRequest request = new TokenRefreshRequest(refreshToken);

    given(jwtProvider.getEmail(refreshToken)).willReturn(email);
    given(redisService.get("refresh:" + email)).willReturn(null); // Redis에 없음

    // when & then
    assertThatThrownBy(() -> authService.refresh(request))
        .isInstanceOf(MemberException.class)
        .hasMessage("유효하지 않은 refresh token입니다.");
  }

  @Test
  @DisplayName("토큰 재발급 실패 - Redis 토큰과 불일치")
  void refresh_fail_token_mismatch() {
    // given
    String refreshToken = "request-refresh-token";
    String email = "test@test.com";
    TokenRefreshRequest request = new TokenRefreshRequest(refreshToken);

    given(jwtProvider.getEmail(refreshToken)).willReturn(email);
    given(redisService.get("refresh:" + email)).willReturn("다른-refresh-token"); // 불일치

    // when & then
    assertThatThrownBy(() -> authService.refresh(request))
        .isInstanceOf(MemberException.class)
        .hasMessage("유효하지 않은 refresh token입니다.");
  }


  // ============================================================
  // 로그아웃
  // ============================================================

  @Test
  @DisplayName("로그아웃 성공")
  void logout_success() {
    // given
    String email = "test@test.com";

    // when
    authService.logout(email);

    // then
    then(redisService).should().delete("refresh:" + email);
  }
}