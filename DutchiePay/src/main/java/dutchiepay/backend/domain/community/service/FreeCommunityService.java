package dutchiepay.backend.domain.community.service;

import com.querydsl.core.Tuple;
import dutchiepay.backend.domain.commerce.repository.CommentRepository;
import dutchiepay.backend.domain.commerce.repository.FreeRepository;
import dutchiepay.backend.domain.community.dto.*;
import dutchiepay.backend.domain.community.exception.CommunityErrorCode;
import dutchiepay.backend.domain.community.exception.CommunityException;
import dutchiepay.backend.domain.community.repository.QFreeRepositoryImpl;
import dutchiepay.backend.domain.user.service.UserUtilService;
import dutchiepay.backend.entity.Free;
import dutchiepay.backend.entity.User;
import dutchiepay.backend.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FreeCommunityService {

    private final QFreeRepositoryImpl qFreeRepository;
    private final FreeRepository freeRepository;
    private final CommentRepository commentRepository;
    private final UserUtilService userUtilService;


    /**
     * 게시글 리스트 조회
     *
     * @return 게시글 리스트 dto
     */
    public FreeListResponseDto getFreeList(String category, String filter, int limit, Long cursor) {

        return qFreeRepository.getFreeLists(category, filter, limit, cursor);
    }

    /**
     * 게시글 단건 조회
     *
     * @param freeId 조회할 게시글 Id
     * @return 게시글 상세 정보 dto
     */
    public FreePostResponseDto getFreePost(Long freeId) {

        Tuple freePost = qFreeRepository.getFreePost(freeId);
        Free free = freeRepository.findById(freeId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.CANNOT_FOUND_POST));
        int count = commentRepository.countCommentByFree(free);

        return FreePostResponseDto.toDto(free.getUser(), free, count);
    }

    /**
     * 게시글 작성
     *
     * @param user                 작성을 요청한 user
     * @param createFreeRequestDto 게시글 내용이 담긴 dto
     * @return 작성 완료한 게시글의 Id를 담은 map
     */
    @Transactional
    public Map<String, Long> createFreePost(User user, CreateFreeRequestDto createFreeRequestDto) {
        String content = createFreeRequestDto.getContent();
        if (content.replaceAll("<[^>]*>", "").length() < 2)
            throw new CommunityException(CommunityErrorCode.INSUFFICIENT_LENGTH);

        Free newPost = freeRepository.save(Free.builder()
                .user(user)
                .title(createFreeRequestDto.getTitle())
                .contents(createFreeRequestDto.getContent())
                .category(createFreeRequestDto.getCategory())
                .postImg(createFreeRequestDto.getThumbnail()).build());
        return Map.of("freeId", newPost.getFreeId());
    }

    /**
     * 게시글 수정
     *
     * @param user                 수정을 요청한 user
     * @param updateFreeRequestDto 수정 내용이 담긴 dto
     */
    @Transactional
    public void updateFreePost(User user, UpdateFreeRequestDto updateFreeRequestDto) {
        String content = updateFreeRequestDto.getContent();
        if (content.replaceAll("<[^>]*>", "").length() < 2)
            throw new CommunityException(CommunityErrorCode.INSUFFICIENT_LENGTH);

        Free free = freeRepository.findById(updateFreeRequestDto.getFreeId())
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.CANNOT_FOUND_POST));
        if (!free.getUser().equals(user)) throw new CommunityException(CommunityErrorCode.UNMATCHED_WRITER);

        free.updateFree(updateFreeRequestDto);
    }

    /**
     * 게시글 삭제
     *
     * @param user   삭제를 요청한 user
     * @param freeId 삭제할 게시글 Id
     */
    @Transactional
    public void deleteFreePost(User user, Long freeId) {
        Free free = freeRepository.findById(freeId)
                .orElseThrow(() -> new CommunityException(CommunityErrorCode.CANNOT_FOUND_POST));
        if (!free.getUser().equals(user)) throw new CommunityException(CommunityErrorCode.UNMATCHED_WRITER);

        free.delete();
    }

    /**
     * 인기게시글/추천게시글 조회
     *
     * @param category 추천 게시글에서 같은 category를 조회하기 위한 파라미터
     * @return 인기/추천게시글 각 5개
     */
    public HotAndRecommendsResponseDto hotAndRecommends(String category) {

        return HotAndRecommendsResponseDto.toDto(qFreeRepository.getHotPosts(),
                qFreeRepository.getRecommendsPosts(category));
    }

    @Transactional
    public void addEntity() {
        String[] categories = new String[]{"free", "info", "hobby", "qna"};
        int index = 1;
        for (Long i = 5L; i < 15L; i++) {
            User user = userUtilService.findById(i);
            for (int j = 1; j < 11; j++) {
                freeRepository.save(Free.builder()
                        .user(user)
                        .title("Test" + index)
                        .contents("Test Contents " + index)
                        .category(categories[j % 4])
                        .postImg(null)
                        .hits(index + j)
                        .description("test description for " + index).build());
                index++;
            }
        }
    }
}
