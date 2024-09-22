package dutchiepay.backend.domain.user.service;

import dutchiepay.backend.domain.user.dto.*;
import dutchiepay.backend.domain.user.exception.UserErrorCode;
import dutchiepay.backend.domain.user.exception.UserErrorException;
import dutchiepay.backend.domain.user.repository.UserRepository;
import dutchiepay.backend.entity.User;
import dutchiepay.backend.global.jwt.JwtUtil;
import dutchiepay.backend.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {

    private final UserUtilService userUtilService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2AuthorizedClientService oauthService;

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String naverClientId;

    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String naverClientSecret;

    @Transactional
    public void signup(UserSignupRequestDto requestDto) {
        String nickname = requestDto.getNickname();
        existsNickname(nickname);

        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        User user = User.builder()
            .email(requestDto.getEmail())
            .password(encodedPassword)
            .phone(requestDto.getPhone())
            .nickname(requestDto.getNickname())
            .username(requestDto.getName())
            .location(requestDto.getLocation())
            .build();

        userRepository.save(user);
    }

    public void existsNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }
    }

    public FindEmailResponseDto findEmail(FindEmailRequestDto req) {
        User user = userUtilService.findByPhone(req.getPhone());

        return FindEmailResponseDto.of(userUtilService.maskEmail(user.getEmail()));
    }

    public void findPassword(FindPasswordRequestDto req) {
        userRepository.findByPhone(req.getPhone())
                .orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void changeNonUserPassword(NonUserChangePasswordRequestDto req) {
        // TODO entity save만으로 PasswordEncoder가 동작하는지 확인 필요
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));

        user.changePassword(req.getPassword());
    }

    @Transactional
    public String changeUserPassword(UserChangePasswordRequestDto req) {
        // TODO 유저 비밀번호 재설정의 경우에는 토큰으로 유저를 파악해서 진행. 추후 구현 필요
        return null;
    }

    public void unlinkKakao(UserDetailsImpl userDetails) {
        OAuth2AuthorizedClient authorizedClient = oauthService.loadAuthorizedClient(
                "kakao", // OAuth2 로그인 제공자 이름 (예: "google", "naver")
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

    public void unlinkNaver(UserDetailsImpl userDetails) {

        OAuth2AuthorizedClient authorizedClient = oauthService.loadAuthorizedClient(
                "naver", // OAuth2 로그인 제공자 이름 (예: "google", "naver")
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
    public void deleteUser(UserDetailsImpl userDetails) {
        userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new UserErrorException(UserErrorCode.USER_EMAIL_NOT_FOUND)).delete();
    }

    public UserLoginResponseDto userInfo(UserDetailsImpl userDetails) {
        User user = userRepository.findByOauthProviderAndEmail(userDetails.getOAuthProvider(), userDetails.getEmail())
                .orElseThrow(() -> new UserErrorException(UserErrorCode.USER_NOT_FOUND));
        final JwtUtil jwtUtil = new JwtUtil();
        String accessToken = jwtUtil.createAccessToken(user.getUserId());

        return UserLoginResponseDto.toDto(user, accessToken);
    }
}
