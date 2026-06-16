import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.boardify.domain.board.dto.BoardRequest;
import com.boardify.domain.board.dto.BoardResponse;
import com.boardify.domain.board.entity.Board;
import com.boardify.domain.board.repository.BoardRepository;
import com.boardify.domain.board.service.BoardService;
import com.boardify.domain.member.entity.Member;
import com.boardify.domain.member.repository.MemberRepository;
import com.boardify.exception.BoardException;
import com.boardify.exception.MemberException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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

  /*
  * 글쓰기
  * */
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

    given(memberRepository.findByEmail("test@test.com")).willReturn(Optional.of(member));
    given(boardRepository.save(any(Board.class))).willReturn(board);

    //when(실행)
    BoardResponse response = boardService.create(request, "test@test.com");

    //then(검증)
    assertThat(response.getTitle()).isEqualTo("제목");
    assertThat(response.getContent()).isEqualTo("내용");
    assertThat(response.getAuthor()).isEqualTo("유진");

    then(memberRepository).should().findByEmail("test@test.com");
    then(boardRepository).should().save(any(Board.class));
  }

  @Test
  @DisplayName("글쓰기 실패 - 멤버 없음")
  void create_fail_member_not_found() {

    //given
    BoardRequest request = new BoardRequest("제목", "내용");
    given(memberRepository.findByEmail("test@test.com")).willReturn(Optional.empty());


    //when & then
    assertThatThrownBy(() -> boardService.create(request, "test@test.com"))
        .isInstanceOf(MemberException.class)
        .hasMessage("Member not found");
  }


  /*
  * 글목록
  * */
  @Test
  @DisplayName("글 목록 불러오기 성공")
  void findAll_success() {

    //given
    Member member = Member.builder()
        .nickname("유진")
        .build();

    Board board = Board.builder()
        .member(member)
        .title("제목")
        .content("내용")
        .build();

    Pageable pageable = (Pageable) PageRequest.of(0, 10);
    Page<Board> boardPage = new PageImpl<>(List.of(board));

    given(boardRepository.findAll(pageable)).willReturn(boardPage);

    //when
    Page<BoardResponse> result = boardService.findAll(pageable);

    //then
    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).getTitle()).isEqualTo("제목");

    then(boardRepository).should().findAll(pageable);


  }

  @Test
  @DisplayName("글 목록 불러오기 - 게시글 없음")
  void findAll_empty() {
    //given
    Pageable pageable = PageRequest.of(0, 10);
    given(boardRepository.findAllByDeletedFalse(pageable)).willReturn(Page.empty());

    //when
    Page<BoardResponse> result = boardService.findAll(pageable);

    //then
    assertThat(result.getContent()).isEmpty();
  }



  /*
  * 글상세
  * */
  @Test
  @DisplayName("글 상세 불러오기 성공")
  void find_success(){

    //given
    Member member = Member.builder()
        .nickname("유진")
        .build();

    Board board = Board.builder()
        .member(member)
        .title("제목")
        .content("내용")
        .build();

    given(boardRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(board));

    //when
    BoardResponse response = boardService.findOne(1L);

    //then
    assertThat(response.getTitle()).isEqualTo("제목");
    assertThat(response.getContent()).isEqualTo("내용");
    assertThat(response.getAuthor()).isEqualTo("유진");

    then(boardRepository).should().findByIdAndDeletedFalse(1L);


  }

  @Test
  @DisplayName("글 상세 불러오기 실패 - 존재하지 않는 게시글")
  void find_fail_not_found() {
    //given
    given(boardRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.empty());

    //when & then
    assertThatThrownBy(() -> boardService.findOne(1L))
        .isInstanceOf(BoardException.class)
        .hasMessage("Board not found.");
  }

  /*
  * 글 수정
  * */
  @Test
  @DisplayName("글 수정 성공")
  void update_success(){

    //given
    Member member = Member.builder()
        .email("test@test.com")  // email 추가
        .nickname("유진")
        .build();

    //수정 전 작성한 내용(nickname 유진 까지)
    Board board = Board.builder()
        .member(member)
        .title("제목")
        .content("내용")
        .build();

    BoardRequest request = new BoardRequest("수정된 제목", "수정된 내용");

    given(boardRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(board));

    //when
    BoardResponse response = boardService.update(1L, request, "test@test.com");

    //then
    assertThat(response.getTitle()).isEqualTo("수정된 제목");
    assertThat(response.getContent()).isEqualTo("수정된 내용");

    then(boardRepository).should().findByIdAndDeletedFalse(1L);


  }

  @Test
  @DisplayName("글 수정 실패 - 존재하지 않는 게시글")
  void update_fail_not_found() {

    //given
    BoardRequest request = new BoardRequest("수정된 제목", "수정된 내용");
    given(boardRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.empty());

    //when & then
    assertThatThrownBy(()-> boardService.update(1L, request, "test@test.com"))
        .isInstanceOf(BoardException.class)
        .hasMessage("Board not found.");


  }


  /*
  * 글삭제
  * */
  @Test
  @DisplayName("글 삭제 성공")
  void delete_success(){

    //given
    Member member = Member.builder()
        .email("test@test.com")  // email 추가
        .nickname("유진")
        .build();

    Board board = Board.builder()
        .member(member)
        .title("제목")
        .content("내용")
        .build();

    given(boardRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.of(board));

    //when
    boardService.delete(1L, "test@test.com");

    //then
    then(boardRepository).should().findByIdAndDeletedFalse(1L);

  }

  @Test
  @DisplayName("글 삭제 실패 - 존재하지 않는 게시글")
  void delete_fail_not_found() {

    //given
    given(boardRepository.findByIdAndDeletedFalse(1L)).willReturn(Optional.empty());

    //when & then
    assertThatThrownBy(()-> boardService.delete(1L, "test@test.com"))
        .isInstanceOf(BoardException.class)
        .hasMessage("Board not found.");

  }




}
