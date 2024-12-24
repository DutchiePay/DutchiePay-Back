package dutchiepay.backend.domain.community.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dutchiepay.backend.domain.community.dto.PurchaseForUpdateDto;
import dutchiepay.backend.domain.community.dto.PurchaseListResponseDto;
import dutchiepay.backend.domain.community.dto.PurchaseResponseDto;
import dutchiepay.backend.domain.community.exception.CommunityErrorCode;
import dutchiepay.backend.domain.community.exception.CommunityException;
import dutchiepay.backend.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class QPurchaseRepositoryImpl {
    private final JPAQueryFactory jpaQueryFactory;
    QPurchase purchase = QPurchase.purchase;
    QUser user = QUser.user;

    public PurchaseListResponseDto getPurchaseList(User u, String category, String word, int limit, Long cursor) {
        // 커서 초기화
        if (cursor == null) cursor = Long.MAX_VALUE;

        BooleanExpression condition = purchase.deletedAt.isNull()
                .and(purchase.purchaseId.loe(cursor));

        // category가 있으면 검색 조건에 category 추가
        if (StringUtils.hasText(category)) {
            condition = condition.and(purchase.category.eq(category));
        }

        // word가 있으면 검색 조건에 word 추가
        if (StringUtils.hasText(word)) {
            condition = condition.and(purchase.title.contains(word));
        }
        condition = condition.and(purchase.location.eq(user != null? u.getLocation() : "서울시 중구"));

        List<PurchaseListResponseDto.PurchaseDetail> result =
                jpaQueryFactory.select(Projections.constructor(PurchaseListResponseDto.PurchaseDetail.class,
                        purchase.purchaseId,
                        user.nickname.as("writer"),
                        user.profileImg.as("writerProfileImg"),
                        purchase.title,
                        purchase.goods,
                        purchase.price,
                        purchase.thumbnail,
                        purchase.meetingPlace,
                        purchase.state,
                        purchase.createdAt,
                        purchase.category
                ))
                .from(purchase)
                .leftJoin(purchase.user, user)
                .where(condition)
                .orderBy(purchase.createdAt.desc())
                .limit(limit + 1)
                .fetch();

        Long nextCursor = result.size() > limit ? result.get(limit).getPurchaseId() : null;
        return PurchaseListResponseDto.builder()
                .posts(result.subList(0, Math.min(result.size(), limit)))
                .cursor(nextCursor).build();

    }

    public PurchaseResponseDto getPurchase(Long purchaseId) {

        PurchaseResponseDto result = jpaQueryFactory.select(Projections.constructor(PurchaseResponseDto.class,
                        user.userId.as("writerId"),
                        user.nickname.as("writer"),
                        user.profileImg.as("writerProfileImage"),
                        purchase.title,
                        purchase.category,
                        purchase.contents,
                        purchase.goods,
                        purchase.price,
                        purchase.meetingPlace,
                        purchase.latitude,
                        purchase.longitude,
                        purchase.state,
                        purchase.createdAt,
                        purchase.hits))
                .from(purchase)
                .where(purchase.purchaseId.eq(purchaseId))
                .fetchFirst();
        if (result == null) throw new CommunityException(CommunityErrorCode.CANNOT_FOUND_POST);
        return result;
    }

    public PurchaseForUpdateDto getPurchaseForUpdate(Long purchaseId) {
        return jpaQueryFactory.select(Projections.fields(PurchaseForUpdateDto.class,
                        purchase.title,
                        purchase.category,
                        purchase.contents,
                        purchase.goods,
                        purchase.price,
                        purchase.meetingPlace,
                        purchase.latitude,
                        purchase.longitude))
                .from(purchase)
                .where(purchase.purchaseId.eq(purchaseId))
                .fetchFirst();
    }
}
