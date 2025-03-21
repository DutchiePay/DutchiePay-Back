package dutchiepay.backend.global.oauth.service;

import dutchiepay.backend.domain.user.exception.UserErrorCode;
import dutchiepay.backend.domain.user.exception.UserErrorException;
import dutchiepay.backend.domain.user.repository.UserRepository;
import dutchiepay.backend.entity.User;
import dutchiepay.backend.global.jwt.redis.RedisService;
import dutchiepay.backend.global.oauth.dto.OAuthAttribute;
import dutchiepay.backend.global.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService oauthService;
    private final RedisService redisService;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttribute oAuthAttribute = OAuthAttribute.of(userNameAttributeName, oAuth2User.getAttributes(), registrationId);

        User user = saveOrUpdate(oAuthAttribute, oAuth2User.getAttributes());

        return new UserDetailsImpl(user, oAuth2User.getAttributes());
    }

    private User saveOrUpdate(OAuthAttribute oAuthAttribute, Map<String, Object> attributes) {
        User user = userRepository.findByOauthProviderAndEmail(oAuthAttribute.getOauthProvider(), oAuthAttribute.getEmail())
                .orElse(oAuthAttribute.toEntity());

        if (!user.getOauthProvider().equals(oAuthAttribute.getOauthProvider())) {
            User otherAccount = User.builder()
                    .email(oAuthAttribute.getEmail())
                    .nickname(user.getNickname())
                    .oauthId(oAuthAttribute.getOauthId())
                    .oauthProvider(oAuthAttribute.getOauthProvider())
                    .build();
            return userRepository.save(otherAccount);
        }

        return userRepository.save(user);
    }

    @Transactional
    public void unlinkKakao(UserDetailsImpl userDetails) {
        OAuth2AuthorizedClient authorizedClient = oauthService.loadAuthorizedClient(
                "kakao", // OAuth2 로그인 제공자 이름
                userDetails.getUsername() // 현재 인증된 사용자
        );
        String kakaoAccess = null;
        if (authorizedClient != null) {
            kakaoAccess = authorizedClient.getAccessToken().getTokenValue();// Access Token 추출
        }
        RestTemplate restTemplate = new RestTemplate();

        // POST 요청으로 데이터 전송
        // HttpHeaders 생성
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + kakaoAccess); // Authorization 헤더 설정

        // HttpEntity에 본문 없이 헤더만 담기
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        // POST 요청 보내기 (request body 없음)
        restTemplate.exchange(
                "https://kapi.kakao.com/v1/user/unlink",  // 요청할 URL
                HttpMethod.POST,                 // HTTP 메서드
                entity,                          // HttpEntity (본문 없음, 헤더만 있음)
                String.class                     // 응답 타입
        );
    }

    @Transactional
    public void unlinkNaver(UserDetailsImpl userDetails) {

        OAuth2AuthorizedClient authorizedClient = oauthService.loadAuthorizedClient(
                "naver", // OAuth2 로그인 제공자 이름
                userDetails.getUsername() // 현재 인증된 사용자
        );

        String naverAccess = null;
        if (authorizedClient != null) {
            naverAccess = authorizedClient.getAccessToken().getTokenValue();// Access Token 추출
        }
        RestTemplate restTemplate = new RestTemplate();

        // POST 요청으로 데이터 전송
        String data = "?client_id=" + naverClientId +
                "&client_secret=" + naverClientSecret +
                "&access_token=" + naverAccess +
                "&service_provider=NAVER" +
                "&grant_type=delete";

        restTemplate.exchange(
                "https://nid.naver.com/oauth2.0/token" + data,  // 요청할 URL
                HttpMethod.POST,                 // HTTP 메서드
                null,                          // HttpEntity
                String.class                     // 응답 타입
        );
    }

    @Transactional
    public void deleteOauthUser(HttpServletRequest request, UserDetailsImpl userDetails) {
        userRepository.findByOauthProviderAndEmail(userDetails.getOAuthProvider(), userDetails.getEmail())
                .orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND)).delete();
        redisService.addBlackList(userDetails.getUserId(), request.getHeader("Authorization"));

    }
}
