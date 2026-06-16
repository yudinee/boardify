package com.boardify.domain.board.service;

import com.boardify.domain.board.dto.BoardRequest;
import com.boardify.domain.board.dto.BoardResponse;
import com.boardify.domain.board.entity.Board;
import com.boardify.domain.board.repository.BoardRepository;
import com.boardify.domain.member.entity.Member;
import com.boardify.domain.member.repository.MemberRepository;
import com.boardify.exception.BoardException;
import com.boardify.exception.MemberException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

  private final BoardRepository boardRepository;
  private final MemberRepository memberRepository;

  /*
  * 글쓰기
  */
  @Transactional
  public BoardResponse create(BoardRequest request, String email){
    Member member = memberRepository.findByEmail(email).orElseThrow(()-> new MemberException("Member not found", HttpStatus.NOT_FOUND));

      // Board.builder() → DB에 저장할 Board 객체 만드는 것
      Board board = Board.builder()
          .member(member) //서버에서 꺼낸 member
          .title(request.getTitle()) //클라이언트한테 받은 값
          .content(request.getContent()) //클라이언트한테 받은 값
          .build();

      return BoardResponse.from(boardRepository.save(board));

  }

  /*
  * 글 목록
   */
  public Page<BoardResponse> findAll(
      @PageableDefault (size = 10, sort = "createdAt", direction = Direction.DESC)
      Pageable pageable){

    return boardRepository.findAllByDeletedFalse(pageable).map(BoardResponse::from); //전체를 묶어서 변환할 때 ::from
  }


  /*
  * 글 상세
  */
  public BoardResponse findOne(Long id){

    Board board = boardRepository.findByIdAndDeletedFalse(id).orElseThrow(()-> new BoardException("Board not found.", HttpStatus.NOT_FOUND));
    return BoardResponse.from(board); //하나씩 변환할 때 .form

  }


  /*
  * 글 수정
  * */
  @Transactional
  public BoardResponse update(Long id, BoardRequest request, String email){
    Board board = boardRepository.findByIdAndDeletedFalse(id).orElseThrow(()-> new BoardException("Board not found.", HttpStatus.NOT_FOUND));

    //본인 글인지 확인
    if(!board.getMember().getEmail().equals(email)){
      throw new BoardException("Email or password incorrect", HttpStatus.FORBIDDEN);
    }

    board.update(request.getTitle(), request.getContent());
    return BoardResponse.from(board);
  }

  /*
   *글 삭제
   */
  @Transactional
  public void delete(Long id, String email){
    Board board = boardRepository.findByIdAndDeletedFalse(id).orElseThrow(()-> new BoardException("Board not found.", HttpStatus.NOT_FOUND));

    // 본인 글인지 확인
    if (!board.getMember().getEmail().equals(email)) {
      throw new BoardException("삭제 권한이 없습니다.", HttpStatus.FORBIDDEN);
    }
    board.delete();
  }
}
