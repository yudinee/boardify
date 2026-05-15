import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.boardify.domain.board.dto.BoardRequest;
import com.boardify.domain.board.dto.BoardResponse;
import com.boardify.domain.board.entity.Board;
import com.boardify.domain.board.repository.BoardRepository;
import com.boardify.domain.board.service.BoardService;
import com.boardify.domain.member.entity.Member;
import com.boardify.domain.member.repository.MemberRepository;
import com.boardify.exception.MemberException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BoardServiceTest {

  //BoardService 객체를 만들어서 아래 @Mock들을 BoardService안에 자동으로 주입해줌
  @InjectMocks
  private BoardService boardService;

  //실제 BoardRepository가 아니라 가짜 BoardRepository, db 없이도 테스트 가능
  @Mock
  private BoardRepository boardRepository;

  @Mock
  private MemberRepository memberRepository;

  @Test
  @DisplayName("글쓰기 성공")
  void create_success() {
    //given(준비)
    Member member = Member.builder()
        .nickname("유진")
        .build();

    BoardRequest request = new BoardRequest("제목", "내용");

    Board board = Board.builder()
        .member(member)
        .title(request.getTitle())
        .content(request.getContent())
        .build();

    given(memberRepository.findById(1L)).willReturn(Optional.of(member));
    given(boardRepository.save(any(Board.class))).willReturn(board);

    //when(실행)
    BoardResponse response = boardService.create(request);

    //then(검증)
    assertThat(response.getTitle()).isEqualTo("제목");
    assertThat(response.getContent()).isEqualTo("내용");
    assertThat(response.getAuthor()).isEqualTo("유진");
  }

  @Test
  @DisplayName("글쓰기 실패 - 멤버 없음")
  void create_fail_member_not_found() {

    //given
    BoardRequest request = new BoardRequest("제목", "내용");
    given(memberRepository.findById(1L)).willReturn(Optional.empty());

    //when & then
    assertThatThrownBy(() -> boardService.create(request))
        .isInstanceOf(MemberException.class)
        .hasMessage("Member not found");
  }


}
