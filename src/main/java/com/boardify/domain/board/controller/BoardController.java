package com.boardify.domain.board.controller;

import com.boardify.domain.board.dto.BoardRequest;
import com.boardify.domain.board.dto.BoardResponse;
import com.boardify.domain.board.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardController {

  private final BoardService boardService;

  /*
  * 글쓰기
  */
  @PostMapping
  public ResponseEntity<BoardResponse>create(@RequestBody BoardRequest request){
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(boardService.create(request));
  }

  /*
  * 글 목록
   */
  @GetMapping
  public ResponseEntity<Page<BoardResponse>> findAll(Pageable pageable){
    return ResponseEntity.ok(boardService.findAll(pageable));
  }

  /*
  * 글 상세
  * */
  @GetMapping("/{id}")
  public ResponseEntity<BoardResponse> findOne(@PathVariable Long id){
    return ResponseEntity.ok(boardService.findOne(id));
  }

  /*
  * 글 수정
  * */
  @PutMapping("/{id}")
  public ResponseEntity<BoardResponse> update(@PathVariable Long id, @RequestBody BoardRequest request){
    return ResponseEntity.ok(boardService.update(id, request));
  }

  /*
  * 글 삭재
  * */
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id){
    boardService.delete(id);
    return ResponseEntity.noContent().build();
  }


}
