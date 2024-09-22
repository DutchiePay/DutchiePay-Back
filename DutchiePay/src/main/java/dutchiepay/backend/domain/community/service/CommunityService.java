package dutchiepay.backend.domain.community.service;

import dutchiepay.backend.domain.commerce.repository.FreeRepository;
import dutchiepay.backend.domain.commerce.repository.ShareRepository;
import dutchiepay.backend.domain.community.dto.*;
import dutchiepay.backend.entity.Free;
import dutchiepay.backend.entity.Share;
import dutchiepay.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommunityService {
    private final ShareRepository shareRepository;
    private final FreeRepository freeRepository;

    public CreateSharePostResponseDto createSharePost(User user, CreateSharePostRequestDto request) {
        String serializedImages = String.join(",", request.getImages());

        Share share = Share.builder()
                .user(user)
                .title(request.getTitle())
                .contents(request.getContent())
                .goods(request.getGoods())
                .price(request.getPrice())
                .meetingPlace(request.getMeetingPlace())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .thumbnail(serializedImages)
                .build();

        shareRepository.save(share);

        return CreateSharePostResponseDto.builder()
                .sharePostId(share.getShareId())
                .build();
    }

    public CreateFreePostResponseDto createFreePost(User user, CreateFreePostRequestDto request) {
        String serializedImages = String.join(",", request.getImages());

        Free newPost = Free.builder()
                .user(user)
                .title(request.getTitle())
                .contents(request.getContent())
                .category(request.getCategory())
                .postImg(serializedImages)
                .build();

        freeRepository.save(newPost);

        return CreateFreePostResponseDto.builder()
                .freePostId(newPost.getFreeId())
                .build();
    }
}
