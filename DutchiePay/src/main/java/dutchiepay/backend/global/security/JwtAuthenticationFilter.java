package dutchiepay.backend.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dutchiepay.backend.domain.user.dto.UserLoginRequestDto;
import dutchiepay.backend.domain.user.exception.UserErrorCode;
import dutchiepay.backend.domain.user.exception.UserErrorException;
import dutchiepay.backend.domain.user.repository.UserRepository;
import dutchiepay.backend.entity.User;
import dutchiepay.backend.global.jwt.JwtUtil;
import dutchiepay.backend.global.jwt.RefreshToken;
import dutchiepay.backend.global.jwt.RefreshTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Slf4j(topic = "로그인 & JWT 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository,
        RefreshTokenRepository refreshTokenRepository,
        PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/users/login", "POST"));
    }

    //로그인 시도
    @Override
    public Authentication attemptAuthentication(
        HttpServletRequest request,
        HttpServletResponse response
    ) throws AuthenticationException {
        try {
            UserLoginRequestDto requestDto = new ObjectMapper().readValue(
                request.getInputStream(),
                UserLoginRequestDto.class
            );

            User user = userRepository.findByEmail(requestDto.getEmail()).orElseThrow(
                () -> new UserErrorException(UserErrorCode.USER_EMAIL_NOT_FOUND)
            );

            if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
                throw new UserErrorException(UserErrorCode.USER_INVALID_PASSWORD);
            }          

        } catch (IOException e) {
            log.error(e.getMessage());
            throw new BadCredentialsException(e.getMessage());
        }
        //TODO 계정이 정지된 경우, 탈퇴한 경우 예외처리 필요?
        /*
        AccountExpiredException: 계정이 만료된 경우
        LockedException: 계정이 잠겨 있는 경우
        DisabledException: 계정이 비활성화된 경우
        */
    }

    //로그인 성공
    @Override
    protected void successfulAuthentication(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain chain,
        Authentication authResult
    ) {
        User user = (User) authResult.getPrincipal();
        String accessToken = jwtUtil.createAccessToken(user.getUserId());
        String refreshToken = jwtUtil.createRefreshToken(user.getUserId());

        RefreshToken refreshEntity = RefreshToken.builder()
            .userId(user.getUserId())
            .tokenString(refreshToken)
            .build();
        refreshTokenRepository.save(refreshEntity);

        response.addHeader("Authorization", "Bearer " + accessToken);
        response.addHeader("Authorization", "Bearer " + refreshToken);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        new UserDetailsImpl(user), null, null
                )
        );
    }

    //로그인 실패
    @Override
    protected void unsuccessfulAuthentication(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException failed
    ) throws IOException {

        log.error(failed.getMessage());

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ObjectNode json = new ObjectMapper().createObjectNode();
        json.put("message", failed.getMessage());
        String newResponse = new ObjectMapper().writeValueAsString(json);

        response.setContentType("application/json");
        response.setContentLength(newResponse.getBytes(StandardCharsets.UTF_8).length);
        response.getOutputStream().write(newResponse.getBytes(StandardCharsets.UTF_8));
    }
}
