package dutchiepay.backend.domain.commerce.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GetBuyResponseDto {
    private String productName;
    private String productImg;
    private String productDetail;
    private Integer originalPrice;
    private Integer salePrice;
    private Integer discountPercent;
    private String storeName;
    private String contactNumber;
    private String representative;
    private String storeAddress;
    private Integer skeleton;
    private Integer nowCount;
    private LocalDate deadline;
    private Integer likeCount;
    private Boolean isLiked;
    private Integer reviewCount;
    private Integer askCount;
    private Integer[] ratingCount;
}
