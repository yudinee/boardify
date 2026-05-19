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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BoardService {

  private final BoardRepository boardRepository;
  private final MemberRepository memberRepository;

  /*
  * 글쓰기
  */
  public BoardResponse create(BoardRequest request){
    Member member = memberRepository.findById(1L).orElseThrow(()-> new MemberException("Member not found"));

    try {
      Board board = Board.builder()
          .member(member) //서버에서 꺼낸 member
          .title(request.getTitle()) //클라이언트한테 받은 값
          .content(request.getContent()) //클라이언트한테 받은 값
          .build();

      return BoardResponse.from(boardRepository.save(board));

    }catch (BoardException e){
      throw new BoardException("게시글이 등록되지 않았습니다.", e);
    }
  }

  /*
  * 글 목록
   */
  public Page<BoardResponse> findAll(
      @PageableDefault (size = 10, sort = "createdAt", direction = Direction.DESC)
      Pageable pageable){

    return boardRepository.findAll(pageable).map(BoardResponse::from);
  }

}
