package dutchiepay.backend.domain.profile.repository;

import dutchiepay.backend.domain.profile.dto.GetMyLikesResponseDto;
import dutchiepay.backend.domain.profile.dto.MyGoodsResponseDto;
import dutchiepay.backend.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QProfileRepository {
    List<GetMyLikesResponseDto> getMyLike(User user, String category);

    List<MyGoodsResponseDto> getMyGoods(User user, Pageable pageable);
}
