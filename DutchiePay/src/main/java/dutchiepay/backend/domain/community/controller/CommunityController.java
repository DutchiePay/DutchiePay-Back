package dutchiepay.backend.domain.community.controller;

import dutchiepay.backend.domain.community.dto.*;
import dutchiepay.backend.domain.community.service.CommunityService;
import dutchiepay.backend.global.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "커뮤니티 API", description = "커뮤니티 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/community")
public class CommunityController {
    private final CommunityService communityService;

    /**
     * GET
     */
    @Operation(summary = "나눔/거래 리스트 조회(구현 중)")
    @GetMapping(value = "/trading", params = {"key", "limit"})
    public ResponseEntity<?> getShareList() {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "나눔/거래 게시글 상세 조회(구현 중)")
    @GetMapping(value = "/trading", params = "sharePostId")
    public ResponseEntity<?> getSharePost() {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "자유게시판 리스트 조회(구현 중)")
    @GetMapping(value = "/free", params = {"category", "key", "limit"})
    public ResponseEntity<?> getFreeList() {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "자유게시판 게시글 상세 조회(구현 중)")
    @GetMapping(value = "/free", params = "freePostId")
    public ResponseEntity<?> getFreePost() {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "인기게시글/추천게시글 조회(구현 중)")
    @GetMapping("/free/recommend")
    public ResponseEntity<?> getRecommendList() {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "자유게시판 댓글 조회")
    @GetMapping(value = "/free/comments", params = {"page", "limit"})
    public ResponseEntity<?> getFreeComments() {
        return ResponseEntity.ok().build();
    }

    /**
     * POST
     */
    @Operation(summary = "나눔/거래 게시글 작성(구현 중)")
    @PostMapping("/trading")
    public ResponseEntity<?> createSharePost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                             @RequestBody CreateSharePostRequestDto request) {
        return ResponseEntity.ok().body(communityService.createSharePost(userDetails.getUser(), request));
    }

    @Operation(summary = "자유게시판 게시글 작성(구현 중)")
    @PostMapping("/free")
    public ResponseEntity<?> createFreePost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @RequestBody CreateFreePostRequestDto request) {
        return ResponseEntity.ok().body(communityService.createFreePost(userDetails.getUser(), request));
    }

    @Operation(summary = "게시글 신고")
    @PostMapping("/report/post")
    public ResponseEntity<?> reportPost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                        @RequestBody ReportPostRequestDto request) {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "댓글 신고")
    @PostMapping("/report/comment")
    public ResponseEntity<?> reportComment(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @RequestBody ReportCommentRequestDto request) {
        return ResponseEntity.ok().build();
    }

    /**
     * PATCH
     */
    @Operation(summary = "나눔/거래 게시글 수정(구현 중)")
    @PatchMapping("/trading")
    public ResponseEntity<?> updateSharePost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                             @RequestBody UpdateSharePostRequestDto request) {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "상태 변경")
    @PatchMapping("/status")
    public ResponseEntity<?> updateStatus(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                          @RequestBody UpdateStatusRequestDto request) {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "자유게시판 게시글 수정(구현 중)")
    @PatchMapping("/free")
    public ResponseEntity<?> updateFreePost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @RequestBody UpdateFreePostRequestDto request) {
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE
     */
    @Operation(summary = "나눔/거래 게시글 삭제(구현 중)")
    @DeleteMapping("/trading")
    public ResponseEntity<?> deleteSharePost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                             @RequestBody DeleteSharePostRequestDto request) {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "자유게시판 게시글 삭제(구현 중)")
    @DeleteMapping("/free")
    public ResponseEntity<?> deleteFreePost(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @RequestBody DeleteFreePostRequestDto request) {
        return ResponseEntity.ok().build();
    }
}
