import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.boardify.domain.member.dto.LoginRequest;
import com.boardify.domain.member.dto.LoginResponse;
import com.boardify.domain.member.dto.SignupRequest;
import com.boardify.domain.member.entity.Member;
import com.boardify.domain.member.repository.MemberRepository;
import com.boardify.domain.member.service.AuthService;
import com.boardify.exception.MemberException;
import com.boardify.jwt.JwtProvider;
import java.util.Optional;
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


  /*
  * 회원가입
  * */
  @Test
  @DisplayName("회원가입 성공")
  void signup_success(){
    //given(준비)
    SignupRequest request = new SignupRequest("test@test.com", "유진", "1234");

    given(memberRepository.existsByEmail(request.getEmail())).willReturn(false);
    given(passwordEncoder.encode(request.getPassword())).willReturn("암호화된 비밀번호");
    given(memberRepository.save(any(Member.class))).willReturn(null);

    //when
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


  @Test
  @DisplayName("로그인 성공")
  void login_success(){

    LoginRequest request = new LoginRequest("test@test.com", "1234");

    Member member = Member.builder()
        .email(request.getEmail())
        .password(request.getPassword())
        .build();

    given(memberRepository.findByEmail(request.getEmail())).willReturn(Optional.of(member));
    given(passwordEncoder.matches(request.getPassword(), member.getPassword())).willReturn(true);
    given(jwtProvider.generateAccessToken(member.getEmail())).willReturn("token");

    //when
    LoginResponse response = authService.login(request);

    //then
    assertThat(response.getAccessToken()).isEqualTo("token");

    then(memberRepository).should().findByEmail(request.getEmail());
    then(passwordEncoder).should().matches(request.getPassword(), member.getPassword());
    then(jwtProvider).should().generateAccessToken(member.getEmail());
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




}





