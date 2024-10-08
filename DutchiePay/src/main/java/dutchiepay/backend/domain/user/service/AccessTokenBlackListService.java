package dutchiepay.backend.domain.user.service;

import dutchiepay.backend.domain.user.repository.AccessTokenBlackListRepository;
import dutchiepay.backend.entity.AccessTokenBlackList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AccessTokenBlackListService {

    private final AccessTokenBlackListRepository accessTokenBlackListRepository;

    @Transactional
    public void addBlackList(String token) {
        AccessTokenBlackList blackList = AccessTokenBlackList.builder()
            .token(token).build();
        accessTokenBlackListRepository.save(blackList);
    }

    public boolean isTokenBlackListed(String token) {
        return accessTokenBlackListRepository.existsByToken(token);
    }
}
