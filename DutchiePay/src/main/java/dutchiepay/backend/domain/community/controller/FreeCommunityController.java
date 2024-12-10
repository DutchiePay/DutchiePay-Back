package dutchiepay.backend.domain.community.controller;

import dutchiepay.backend.domain.community.dto.*;
import dutchiepay.backend.domain.community.service.FreeCommunityService;
import dutchiepay.backend.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/free")
public class FreeCommunityController {
    private final FreeCommunityService freeCommunityService;

    @Operation(summary = "자유게시판 리스트 조회(구현중)")
    @GetMapping("/list")
    public ResponseEntity<FreeListResponseDto> getFreePList(@RequestParam(value = "category", required = false) String category,
                                                            @RequestParam("filter") String filter,
                                                            @RequestParam("limit") int limit,
                                                            @RequestParam(value = "cursor", required = false) Long cursor) {
        return ResponseEntity.ok(freeCommunityService.getFreeList(category, filter, limit, cursor));
    }

    @Operation(summary = "자유게시판 상세 조회(구현중)")
    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public FreePostResponseDto getFreePost(@RequestParam("freeId") Long freeId) {

        return freeCommunityService.getFreePost(freeId);
    }

    @Operation(summary = "자유게시판 글 작성(구현중)")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> createFreePost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                            @Valid @RequestBody CreateFreeRequestDto createFreeRequestDto) {
        return ResponseEntity.ok(freeCommunityService.createFreePost(userDetails.getUser(), createFreeRequestDto));
    }

    @Operation(summary = "자유게시판 글 수정(구현중)")
    @PatchMapping
    @PreAuthorize("isAuthenticated()")
    public void updateFreePost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                               @Valid @RequestBody UpdateFreeRequestDto updateFreeRequestDto) {

        freeCommunityService.updateFreePost(userDetails.getUser(), updateFreeRequestDto);
    }

    @Operation(summary = "자유게시판 글 삭제(구현중)")
    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public void deleteFree(@AuthenticationPrincipal UserDetailsImpl userDetails,
                           @RequestParam("freeId") Long freeId) {
        freeCommunityService.deleteFreePost(userDetails.getUser(), freeId);
    }

    @Operation(summary = "자유게시판 인기/추천 게시글 조회(구현중)")
    @GetMapping("/recommend")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<HotAndRecommendsResponseDto> hotAndRecommends(@RequestParam("category") String category) {
        return ResponseEntity.ok(freeCommunityService.hotAndRecommends(category));
    }

    @Operation(summary = "댓글 조회")
    @GetMapping("/comments/list")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponseDto> getComments(@RequestParam("freeId") Long freeId,
                                                          @RequestParam(value = "cursor", required = false) Long cursor,
                                                          @RequestParam("limit") int limit) {
        return ResponseEntity.ok(freeCommunityService.getComments(freeId, cursor, limit));
    }

    @Operation(summary = "답글 조회")
    @GetMapping("/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReCommentResponseDto>> getReComments(@RequestParam("commentId") Long commentId,
                                                                    @RequestParam("type") String type) {
        return ResponseEntity.ok(freeCommunityService.getReComments(commentId, type));
    }

    @Operation(summary = "댓글 작성(구현중)")
    @PostMapping("/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentCreateResponseDto> createComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                  @Valid @RequestBody CommentCreateRequestDto commentRequestDto) {
        return ResponseEntity.ok(freeCommunityService.createComment(userDetails.getUser(), commentRequestDto));
    }

    @Operation(summary = "댓글 수정(구현중)")
    @PatchMapping("/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @Valid @RequestBody CommentUpdateRequestDto updateCommentDto) {
        freeCommunityService.updateComment(userDetails.getUser(), updateCommentDto);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "댓글 삭제(구현중)")
    @DeleteMapping("/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                              @RequestParam("commentId") Long commentId) {
        freeCommunityService.deleteComment(userDetails.getUser(), commentId);
        return ResponseEntity.notFound().build();
    }
}
