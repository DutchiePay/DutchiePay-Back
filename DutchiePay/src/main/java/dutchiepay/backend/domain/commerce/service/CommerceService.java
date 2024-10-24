package dutchiepay.backend.domain.commerce.service;

import dutchiepay.backend.domain.commerce.dto.GetBuyListResponseDto;
import dutchiepay.backend.domain.commerce.dto.GetBuyResponseDto;
import dutchiepay.backend.domain.commerce.dto.GetProductReviewResponseDto;
import dutchiepay.backend.domain.commerce.dto.AddEntityDto;
import dutchiepay.backend.domain.commerce.dto.PaymentInfoResponseDto;
import dutchiepay.backend.domain.commerce.exception.CommerceErrorCode;
import dutchiepay.backend.domain.commerce.exception.CommerceException;
import dutchiepay.backend.domain.commerce.repository.BuyCategoryRepository;
import dutchiepay.backend.domain.commerce.repository.BuyRepository;
import dutchiepay.backend.domain.commerce.repository.CategoryRepository;
import dutchiepay.backend.domain.commerce.repository.StoreRepository;
import dutchiepay.backend.domain.order.repository.AskRepository;
import dutchiepay.backend.domain.order.repository.LikesRepository;
import dutchiepay.backend.domain.order.repository.ProductRepository;
import dutchiepay.backend.entity.*;
import dutchiepay.backend.global.converter.BuyCategoryConverter;
import dutchiepay.backend.global.security.UserDetailsImpl;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class CommerceService {

    private final BuyRepository buyRepository;
    private final LikesRepository likesRepository;
    private final AskRepository askRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final BuyCategoryRepository buyCategoryRepository;

    /**
     * 상품 좋아요
     * @param userDetails 사용자
     * @param buyId 좋아요 할 상품
     */
    @Transactional
    public void likes(UserDetailsImpl userDetails, Long buyId) {
        Buy buy = buyRepository.findById(buyId)
                .orElseThrow(() -> new CommerceException(CommerceErrorCode.CANNOT_FOUND_PRODUCT));
        Likes likes = likesRepository.findByUserAndBuy(userDetails.getUser(), buy);
        if (likes == null) {
            likesRepository.save(Likes.builder().user(userDetails.getUser()).buy(buy).build());
        } else {
            likesRepository.delete(likes);
        }
    }

    /**
     * 상품의 문의 내역 목록을 조회
     * Pagination 구현
     * @param buyId 조회할 상품의 게시글 Id
     * @param pageable pageable 객체
     * @return BuyAskResponseDto 문의 내역 dto
     */
    public Page<Ask> getBuyAsks(Long buyId, Pageable pageable) {

        return askRepository.findByBuyAndDeletedAtIsNull(buyRepository.findById(buyId)
                .orElseThrow(() -> new CommerceException(CommerceErrorCode.CANNOT_FOUND_PRODUCT)), pageable);
    }


    public GetBuyResponseDto getBuyPage(User user, Long buyId) {
        return buyRepository.getBuyPageByBuyId(user.getUserId(), buyId);
    }

    public GetBuyListResponseDto getBuyList(User user, String filter, String category, int end, Long cursor, int limit) {
        return buyRepository.getBuyList(user, filter, category, end, cursor, limit);
    }

    public GetProductReviewResponseDto getProductReview(Long productId, Long photo, Long page, Long limit) {
        if (!productRepository.existsById(productId)) {
            throw new CommerceException(CommerceErrorCode.CANNOT_FOUND_PRODUCT);
        }
        return buyRepository.getProductReview(productId, photo, PageRequest.of(page.intValue() - 1, limit.intValue()));
    }

    /**
     * 공동구매 게시글의 상품 정보 반환
     * @param buyId 상품의 게시글 Id
     * @return PaymentInfoResponseDto 상품의 특정 정보만 담을 dto
     */
    public PaymentInfoResponseDto getPaymentInfo(Long buyId) {
        Buy buy = buyRepository.findById(buyId)
                .orElseThrow(() -> new CommerceException(CommerceErrorCode.CANNOT_FOUND_PRODUCT));
        if (buy.getDeadline().isBefore(LocalDate.now())) throw new CommerceException(CommerceErrorCode.AFTER_DUE_DATE);

        return PaymentInfoResponseDto.toDto(buy);
    }

    @Transactional
    public void addEntity(AddEntityDto addEntityDto) {
        Store store = storeRepository.findByStoreName(addEntityDto.getStoreName());
        if (store == null) store = storeRepository.save(Store.builder()
                        .storeName(addEntityDto.getStoreName())
                        .contactNumber(addEntityDto.getContactNumber())
                        .representative(addEntityDto.getRepresentative())
                        .storeAddress(addEntityDto.getStoreAddress()).build());

        Product product = productRepository.save(Product.builder()
                .store(store)
                .productName(addEntityDto.getProductName())
                .detailImg(addEntityDto.getDetailImg())
                .originalPrice(addEntityDto.getOriginalPrice())
                .salePrice(addEntityDto.getSalePrice())
                .discountPercent(addEntityDto.getDiscountPercent())
                .productImg(addEntityDto.getProductImg()).build());

        Buy buy = buyRepository.save(Buy.builder()
                .product(product)
                .title(addEntityDto.getProductName())
                .deadline(addEntityDto.getDeadline())
                .skeleton(addEntityDto.getSkeleton())
                .nowCount(0)
                .build());

        for (String c : addEntityDto.getCategory()) {
            Category category = categoryRepository.findByName(c);
            if (category == null)
                category = categoryRepository.save(Category.builder().name(c).build());

            buyCategoryRepository.save(BuyCategory.builder()
                    .buy(buy)
                    .category(category)
                    .build());
        }
    }
}
