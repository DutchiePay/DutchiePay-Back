package dutchiepay.backend;

import dutchiepay.backend.domain.commerce.BuyCategory;
import dutchiepay.backend.domain.commerce.repository.*;
import dutchiepay.backend.domain.coupon.repository.CouponRepository;
import dutchiepay.backend.domain.coupon.repository.UsersCouponRepository;
import dutchiepay.backend.domain.order.repository.LikesRepository;
import dutchiepay.backend.domain.order.repository.OrdersRepository;
import dutchiepay.backend.domain.order.repository.ProductRepository;
import dutchiepay.backend.domain.profile.dto.GetMyLikesResponseDto;
import dutchiepay.backend.domain.profile.dto.MyGoodsResponseDto;
import dutchiepay.backend.domain.profile.dto.MyPageResponseDto;
import dutchiepay.backend.domain.profile.dto.MyPostsResponseDto;
import dutchiepay.backend.domain.profile.repository.ProfileRepository;
import dutchiepay.backend.domain.profile.service.ProfileService;
import dutchiepay.backend.domain.user.repository.UserRepository;
import dutchiepay.backend.entity.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@ActiveProfiles("prod")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@SpringBootTest
class ProfileRepositoryTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BuyRepository buyRepository;

    @Autowired
    private LikesRepository likesRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private FreeRepository freeRepository;

    @Autowired
    private ShareRepository shareRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private UsersCouponRepository usersCouponRepository;

    @Autowired
    private ProfileService profileService;


    @BeforeEach
    public void setUp() {
    }

    @Test
    @Transactional
    void 마이페이지조회() {
        // given
        // 유저
        User user = User.builder()
                .email("test@example.com")
                .username("test")
                .nickname("더취3")
                .location("서울시 마포구")
                .phone("01012345678")
                .state(0)
                .build();

        userRepository.save(user);

        // 업체
        Store store = Store.builder()
                .storeName("테스트 업체")
                .contactNumber("024343434")
                .representative("테스트 대표")
                .storeAddress("서울시 마포구")
                .build();
        storeRepository.save(store);

        // 상품
        Product product1 = Product.builder()
                .storeId(store)
                .productName("테스트 상품1")
                .detailImg("테스트 상세 이미지1")
                .originalPrice(10000)
                .salePrice(9000)
                .discountPercent(10)
                .productImg("테스트 상품 이미지1")
                .build();

        Product product2 = Product.builder()
                .storeId(store)
                .productName("테스트 상품2")
                .detailImg("테스트 상세 이미지2")
                .originalPrice(10000)
                .salePrice(9000)
                .discountPercent(10)
                .productImg("테스트 상품 이미지2")
                .build();
        productRepository.save(product1);
        productRepository.save(product2);

        // 공구게시글
        Buy buy1 = Buy.builder()
                .productId(product1)
                .title("테스트 공구 게시글1")
                .deadline(LocalDate.now())
                .skeleton(10)
                .nowCount(100)
                .category(BuyCategory.가전)
                .build();

        Buy buy2 = Buy.builder()
                .productId(product2)
                .title("테스트 공구 게시글2")
                .deadline(LocalDate.now())
                .skeleton(10)
                .nowCount(100)
                .category(BuyCategory.가전)
                .build();
        buyRepository.save(buy1);
        buyRepository.save(buy2);

        Coupon coupon1 = Coupon.builder()
                .couponName("테스트 쿠폰1")
                .expireDate(LocalDate.now().plusDays(7))
                .percentage(10)
                .requirePrice(10000)
                .build();

        Coupon coupon2 = Coupon.builder()
                .couponName("테스트 쿠폰2")
                .expireDate(LocalDate.now().plusDays(7))
                .percentage(10)
                .requirePrice(10000)
                .build();

        Coupon coupon3 = Coupon.builder()
                .couponName("테스트 쿠폰3")
                .expireDate(LocalDate.now().plusDays(7))
                .percentage(10)
                .requirePrice(10000)
                .build();
        couponRepository.save(coupon1);
        couponRepository.save(coupon2);
        couponRepository.save(coupon3);

        // 유저 쿠폰
        Users_Coupon users_coupon1 = Users_Coupon.builder()
                .user(user)
                .coupon(coupon1)
                .build();

        Users_Coupon users_coupon2 = Users_Coupon.builder()
                .user(user)
                .coupon(coupon2)
                .build();

        Users_Coupon users_coupon3 = Users_Coupon.builder()
                .user(user)
                .coupon(coupon3)
                .build();
        usersCouponRepository.save(users_coupon1);
        usersCouponRepository.save(users_coupon2);
        usersCouponRepository.save(users_coupon3);

        // 주문
        Orders orders1 = Orders.builder()
                .user(user)
                .product(product1)
                .buy(buy1)
                .orderNum("2021090001")
                .totalPrice(9000)
                .payment("카드결제")
                .orderedAt(LocalDateTime.now())
                .address("연세대학교 1층 로비")
                .state("결제 완료")
                .amount(2)
                .build();
        ordersRepository.save(orders1);

        // when
        MyPageResponseDto dto = profileService.myPage(user);

        // then
        System.out.println("주소 = " + dto.getAddress());
        System.out.println("상세주소 = " + dto.getDetail());
        System.out.println("우편번호 = " + dto.getZipcode());
        System.out.println("전화번호 = " + dto.getPhone());
        System.out.println("쿠폰 수 = " + dto.getCoupon());
        System.out.println("주문 수 = " + dto.getOrder());
        System.out.println("이메일 = " + dto.getEmail());
    }

    @Test
    @Transactional
    void 좋아요누른상품찾기() {
        // given
        // 유저
        User user = User.builder()
                .email("test@example.com")
                .username("test")
                .nickname("더취3")
                .location("서울시 마포구")
                .phone("01012345678")
                .state(0)
                .build();

        userRepository.save(user);

        // 업체
        Store store = Store.builder()
                .storeName("테스트 업체")
                .contactNumber("024343434")
                .representative("테스트 대표")
                .storeAddress("서울시 마포구")
                .build();
        storeRepository.save(store);

        // 상품
        Product product = Product.builder()
                .storeId(store)
                .productName("테스트 상품")
                .detailImg("테스트 상세 이미지")
                .originalPrice(10000)
                .salePrice(9000)
                .discountPercent(10)
                .productImg("테스트 상품 이미지")
                .build();
        productRepository.save(product);

        // 공구게시글
        Buy buy = Buy.builder()
                .productId(product)
                .title("테스트 공구 게시글1")
                .deadline(LocalDate.now())
                .skeleton(10)
                .nowCount(100)
                .category(BuyCategory.가전)
                .build();
        buyRepository.save(buy);

        Likes like = Likes.builder()
                .user(user)
                .buy(buy)
                .build();
        likesRepository.save(like);

        Score score = Score.builder()
                .buy(buy)
                .one(1)
                .two(2)
                .three(3)
                .four(4)
                .five(5)
                .count(15)
                .build();
        scoreRepository.save(score);


        // when
        List<GetMyLikesResponseDto> result = profileRepository.getMyLike(user, "디지털/가전");
        
        // then
        GetMyLikesResponseDto likeResult = result.get(0);
        System.out.println("카테고리 = " + likeResult.getCategory());
        System.out.println("제목 = " + likeResult.getTitle());
        System.out.println("원래 가격 = " + likeResult.getOriginalPrice());
        System.out.println("세일 가격 = " + likeResult.getSalePrice());
        System.out.println("할인율 = " + likeResult.getDiscountPercent());
        System.out.println("썸네일 = " + likeResult.getThumbnail());
        System.out.println("평균 별점 = " + likeResult.getAverage());
        System.out.println("리뷰 수 = " + likeResult.getReviewCount());
        System.out.println("마감 날짜 = " + likeResult.getExpireDate());
    }

    @Test
    @Transactional
    void 구매내역조회() {
        // given
        // 유저
        User user = User.builder()
                .email("test@example.com")
                .username("test")
                .nickname("더취3")
                .location("서울시 마포구")
                .phone("01012345678")
                .state(0)
                .build();

        userRepository.save(user);

        // 업체
        Store store = Store.builder()
                .storeName("테스트 업체")
                .contactNumber("024343434")
                .representative("테스트 대표")
                .storeAddress("서울시 마포구")
                .build();
        storeRepository.save(store);

        // 상품
        Product product1 = Product.builder()
                .storeId(store)
                .productName("테스트 상품")
                .detailImg("테스트 상세 이미지")
                .originalPrice(10000)
                .salePrice(9000)
                .discountPercent(10)
                .productImg("테스트 상품 이미지")
                .build();

        Product product2 = Product.builder()
                .storeId(store)
                .productName("테스트 상품2")
                .detailImg("테스트 상세 이미지2")
                .originalPrice(5000)
                .salePrice(4500)
                .discountPercent(10)
                .productImg("테스트 상품 이미지2")
                .build();
        productRepository.save(product1);
        productRepository.save(product2);

        // 공구 게시글
        Buy buy1 = Buy.builder()
                .productId(product1)
                .title("테스트 공구 게시글1")
                .deadline(LocalDate.now())
                .skeleton(10)
                .nowCount(100)
                .category(BuyCategory.가전)
                .build();

        Buy buy2 = Buy.builder()
                .productId(product2)
                .title("테스트 공구 게시글1")
                .deadline(LocalDate.now())
                .skeleton(10)
                .nowCount(100)
                .category(BuyCategory.가전)
                .build();
        buyRepository.save(buy1);
        buyRepository.save(buy2);


        // 주문
        Orders orders1 = Orders.builder()
                .user(user)
                .product(product1)
                .buy(buy1)
                .orderNum("2021090001")
                .amount(1)
                .totalPrice(18000)
                .payment("카드결제")
                .orderedAt(LocalDateTime.now())
                .address("연세대학교 1층 로비")
                .state("0")
                .amount(2)
                .build();

        Orders orders2 = Orders.builder()
                .user(user)
                .product(product2)
                .buy(buy2)
                .orderNum("2021090002")
                .amount(1)
                .totalPrice(22500)
                .payment("카드결제")
                .orderedAt(LocalDateTime.now())
                .address("지나가던 버스 정류장 앞")
                .state("0")
                .amount(5)
                .build();
        ordersRepository.save(orders1);
        ordersRepository.save(orders2);

        // when
        List<MyGoodsResponseDto> result = profileRepository.getMyGoods(user, PageRequest.of(0, 1));

        // then
        for (MyGoodsResponseDto dto : result) {
            System.out.println("주문Id = " + dto.getOrderId());
            System.out.println("주문 번호 = " + dto.getOrderNum());
            System.out.println("상품Id = " + dto.getProductId());
            System.out.println("주문 날짜/시간 = " + dto.getOrderDate());
            System.out.println("상품 이름 = " + dto.getProductName());
            System.out.println("수량 = " + dto.getCount());
            System.out.println("상품 가격 = " + dto.getProductPrice());
            System.out.println("총 가격 = " + dto.getTotalPrice());
            System.out.println("할인율 = " + dto.getDiscountPercent());
            System.out.println("결제 방법 = " + dto.getPayment());
            System.out.println("주문 배송지 = " + dto.getDeliveryAddress());
            System.out.println("배송 상태 = " + dto.getDeliveryState());
            System.out.println("상품 이미지 = " + dto.getProductImg());
            System.out.println("업체 이름 = " + dto.getStoreName());
            System.out.println("=============================================================");
        }

    }

    @Test
    @Transactional
    void 내가작성한글조회() {
        // given
        // 유저
        User user = User.builder()
                .email("test@example.com")
                .username("test")
                .nickname("더취3")
                .location("서울시 마포구")
                .phone("01012345678")
                .state(0)
                .build();

        userRepository.save(user);

        // 자유 게시글
        Free freePost1 = Free.builder()
                .user(user)
                .title("테스트 자유 게시글1")
                .contents("테스트 자유 게시글 내용1")
                .category("자유")
                .hits(0)
                .build();

        Free freePost2 = Free.builder()
                .user(user)
                .title("테스트 자유 게시글2")
                .contents("테스트 자유 게시글 내용2")
                .category("자유")
                .hits(0)
                .build();
        freeRepository.save(freePost1);
        freeRepository.save(freePost2);

        // 마트/배달 게시글
        Share sharePost1 = Share.builder()
                .user(user)
                .title("테스트 마트/배달 게시글1")
                .contents("테스트 마트/배달 게시글 내용1")
                .category("배달")
                .location("지구 어딘가")
                .state("진행중")
                .maximum(10)
                .hits(0)
                .latitude("37.123456")
                .longitude("126.123456")
                .meetingPlace("지구 어딘가")
                .build();
        shareRepository.save(sharePost1);

        // when
        List<MyPostsResponseDto> result = profileRepository.getMyPosts(user, PageRequest.of(0, 10));
        System.out.println("result = " + result);

        // then
        for (MyPostsResponseDto dto : result) {
            System.out.println("게시글Id = " + dto.getPostId());
            System.out.println("제목 = " + dto.getTitle());
            System.out.println("작성 시간 = " + dto.getWriteTime());
            System.out.println("내용 = " + dto.getContent());
            System.out.println("카테고리 = " + dto.getCategory());
            System.out.println("댓글 수 = " + dto.getCommentCount());
            System.out.println("썸네일 = " + dto.getThumbnail());
            System.out.println("=============================================================");
        }
    }

    @Test
    @Transactional
    void 내가작성한댓글조회() {
        // given
        // 유저
        User user = User.builder()
                .email("test@example.com")
                .username("test")
                .nickname("더취3")
                .location("서울시 마포구")
                .phone("01012345678")
                .state(0)
                .build();

        userRepository.save(user);

        // 자유 게시글
        Free freePost1 = Free.builder()
                .user(user)
                .title("테스트 자유 게시글1")
                .contents("테스트 자유 게시글 내용1")
                .category("자유")
                .hits(0)
                .build();

        Free freePost2 = Free.builder()
                .user(user)
                .title("테스트 자유 게시글2")
                .contents("테스트 자유 게시글 내용2")
                .category("자유")
                .hits(0)
                .build();
        freeRepository.save(freePost1);
        freeRepository.save(freePost2);

        // 댓글
        Comment comment1 = Comment.builder()
                .freeId(freePost1)
                .userId(user)
                .contents("테스트 댓글 내용1")
                .build();

        Comment comment2 = Comment.builder()
                .freeId(freePost2)
                .userId(user)
                .contents("테스트 댓글 내용2")
                .build();
        commentRepository.save(comment1);
        commentRepository.save(comment2);

        // when
        List<MyPostsResponseDto> result = profileRepository.getMyCommentsPosts(user, PageRequest.of(0, 10));

        // then
        for (MyPostsResponseDto dto : result) {
            System.out.println("게시글Id = " + dto.getPostId());
            System.out.println("제목 = " + dto.getTitle());
            System.out.println("작성 시간 = " + dto.getWriteTime());
            System.out.println("내용 = " + dto.getContent());
            System.out.println("카테고리 = " + dto.getCategory());
            System.out.println("댓글 수 = " + dto.getCommentCount());
            System.out.println("썸네일 = " + dto.getThumbnail());
            System.out.println("=============================================================");
        }
    }
}
