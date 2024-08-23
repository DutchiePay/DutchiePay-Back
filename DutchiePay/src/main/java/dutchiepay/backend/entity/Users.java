package dutchiepay.backend.entity;

import dutchiepay.backend.global.config.Auditing;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Table(name = "Users")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Users extends Auditing {

    @Id
    @GenerateValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    //이메일
    @Column(nullable = false, length = 255)
    private String emaill;

    //성함
    @Column(length = 5)
    private String username;

    //휴대폰 번호
    @Column(nullable = false, length = 11)
    private String phone;

    //비밀번호
    @Column(length = 255)
    private String password;

    //닉네임
    @Column(nullable = false, length = 8, unique = true)
    private String nickname;

    //지역
    @Column(nullable = false, length = 15)
    private String location;

    //주소
    @Column(length = 255)
    private String address;

    //상세 주소
    @Column(length = 255)
    private String detail;

    //리프레쉬 토큰
    @Column(length = 512)
    private String refreshToken;

    //프로필 이미지
    @Column(length = 255)
    private String profileImg;

    //탈퇴&정지 여부
    //0 : 정상, 1 : 정지, 2 : 탈퇴
    @Column(nullable = false)
    private Integer state;

    //소셜 ID
    @Column(length = 20)
    private String oauth_id;

    //소셜 종류
    @Column(length = 10)
    private String oauth_provider;

}