package dutchiepay.backend.domain.search.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dutchiepay.backend.domain.commerce.dto.GetBuyListResponseDto;
import dutchiepay.backend.domain.commerce.exception.CommerceErrorCode;
import dutchiepay.backend.domain.commerce.exception.CommerceException;
import dutchiepay.backend.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class QSearchRepositoryImpl implements QSearchRepository {

    private final JPAQueryFactory jpaQueryFactory;

    QBuy buy = QBuy.buy;
    QProduct product = QProduct.product;
    QReview review = QReview.review;
    QLike like = QLike.like;
    QScore score = QScore.score;
    QBuyCategory buyCategory = QBuyCategory.buyCategory;
    QCategory category = QCategory.category;

    @Override
    public GetBuyListResponseDto searchCommerce(User user, String filter, String keyword, int end, Long cursor, int limit) {
        if (cursor == null) {
            cursor = Long.MAX_VALUE;
        }

        BooleanExpression conditions = null;

        if (end == 0) {
            BooleanExpression dateCondition = buy.deadline.after(LocalDate.now().minusDays(1));
            conditions = conditions == null ? dateCondition : conditions.and(dateCondition);
        }

        OrderSpecifier[] orderBy;
        BooleanExpression cursorCondition = null;
        switch (filter) {
            case "like":
                orderBy = new OrderSpecifier[]{like.count().desc(), buy.buyId.desc()};
                if (cursor < Long.MAX_VALUE) {
                    Long cursorLike = jpaQueryFactory
                            .select(like.count())
                            .from(like)
                            .join(like.buy, buy)
                            .where(buy.buyId.eq(cursor))
                            .fetchOne();

                    if (cursorLike != null) {
                        cursorCondition = like.count().lt(cursorLike)
                                .or(like.count().eq(cursorLike))
                                .and(buy.buyId.loe(cursor));
                    }
                }
                break;
            case "endDate":
                if (cursor < Long.MAX_VALUE) {
                    Tuple cursorInfo = jpaQueryFactory
                            .select(buy.deadline,
                                    Expressions.cases()
                                            .when(buy.deadline.goe(LocalDate.now()))
                                            .then(0)
                                            .otherwise(1))
                            .from(buy)
                            .where(buy.buyId.eq(cursor))
                            .fetchOne();

                    if (cursorInfo != null) {
                        LocalDate cursorEndDate = cursorInfo.get(0, LocalDate.class);
                        Integer cursorGroup = cursorInfo.get(1, Integer.class);

                        BooleanExpression sameGroupCondition = Expressions.cases()
                                .when(buy.deadline.goe(LocalDate.now()))
                                .then(0)
                                .otherwise(1)
                                .eq(cursorGroup)
                                .and(buy.deadline.gt(cursorEndDate)
                                        .or(buy.deadline.eq(cursorEndDate)
                                                .and(buy.buyId.loe(cursor))));

                        BooleanExpression nextGroupCondition = Expressions.cases()
                                .when(buy.deadline.goe(LocalDate.now()))
                                .then(0)
                                .otherwise(1)
                                .gt(cursorGroup);

                        cursorCondition = sameGroupCondition.or(nextGroupCondition);
                    }
                }

                orderBy = new OrderSpecifier[]{
                        Expressions.cases()
                                .when(buy.deadline.goe(LocalDate.now()))
                                .then(0)
                                .otherwise(1)
                                .asc(),
                        buy.deadline.asc(),
                        buy.buyId.desc()
                };
                break;
            case "discount":
                if (cursor < Long.MAX_VALUE) {
                    Integer cursorDiscount = jpaQueryFactory
                            .select(product.discountPercent)
                            .from(buy)
                            .join(buy.product, product)
                            .where(buy.buyId.eq(cursor))
                            .fetchOne();

                    if (cursorDiscount != null) {
                        cursorCondition = product.discountPercent.lt(cursorDiscount)
                                .or(product.discountPercent.eq(cursorDiscount));
                    }
                }
                orderBy = new OrderSpecifier[]{product.discountPercent.desc(), buy.buyId.desc()};
                break;
            case "newest":
                orderBy = new OrderSpecifier[]{buy.buyId.desc()};
                conditions = conditions != null ? conditions.and(buy.buyId.loe(cursor)) : buy.buyId.loe(cursor);
                break;
            default:
                throw new CommerceException(CommerceErrorCode.INVALID_FILTER);
        }

        JPAQuery<Tuple> query = jpaQueryFactory
                .select(buy.buyId,
                        product.productName,
                        product.productImg,
                        product.originalPrice,
                        product.salePrice,
                        product.discountPercent,
                        buy.skeleton,
                        buy.nowCount,
                        buy.deadline,
                        user != null ? JPAExpressions
                                .selectOne()
                                .from(like)
                                .where(like.buy.eq(buy)
                                        .and(like.user.eq(user)))
                                .exists()
                                : Expressions.constant(false),
                        JPAExpressions
                                .select(review.count())
                                .from(review)
                                .where(review.order.buy.eq(buy))
                                .where(review.deletedAt.isNull()))
                .from(buy)
                .join(buy.product, product)
                .leftJoin(buyCategory).on(buyCategory.buy.eq(buy))
                .leftJoin(category).on(buyCategory.category.eq(category))
                .leftJoin(review).on(review.order.buy.eq(buy))
                .leftJoin(like).on(like.buy.eq(buy));

        query.where(conditions, buy.tags.contains(keyword).or(buy.title.contains(keyword)))
                .groupBy(buy.buyId, product.productName, product.productImg, product.originalPrice,
                        product.salePrice, product.discountPercent, buy.skeleton, buy.nowCount, buy.deadline)
                .limit(limit + 1);

        if (filter.equals("like")) {
            query.having(cursorCondition);
            query.orderBy(orderBy);
        } else {
            query.where(cursorCondition);
            query.orderBy(orderBy);
        }

        List<Tuple> results = query.fetch();

        List<Long> buyIds = results.stream()
                .map(result -> result.get(0, Long.class))
                .toList();

        List<Tuple> ratingCountsList = jpaQueryFactory
                .select(
                        score.buy.buyId,
                        score.one,
                        score.two,
                        score.three,
                        score.four,
                        score.five,
                        score.count
                )
                .from(score)
                .where(score.buy.buyId.in(buyIds))
                .groupBy(score.buy.buyId)
                .fetch();

        Map<Long, Tuple> ratingMap = new HashMap<>();
        for (Tuple tuple : ratingCountsList) {
            Long buyId = tuple.get(score.buy.buyId);
            ratingMap.put(buyId, tuple);
        }

        List<GetBuyListResponseDto.ProductDto> products = new ArrayList<>();
        int count = 0;
        for (Tuple result : results) {
            if (count >= limit) {
                break;
            }

            Long buyId = result.get(0, Long.class);
            Tuple ratingCounts = ratingMap.getOrDefault(buyId, null);

            double average = 0.0;

            if (ratingCounts != null) {
                Integer one = Optional.ofNullable(ratingCounts.get(1, Integer.class)).orElse(0);
                Integer two = Optional.ofNullable(ratingCounts.get(2, Integer.class)).orElse(0);
                Integer three = Optional.ofNullable(ratingCounts.get(3, Integer.class)).orElse(0);
                Integer four = Optional.ofNullable(ratingCounts.get(4, Integer.class)).orElse(0);
                Integer five = Optional.ofNullable(ratingCounts.get(5, Integer.class)).orElse(0);
                Integer total = Optional.ofNullable(ratingCounts.get(6, Integer.class)).orElse(0);

                if (total != 0) {
                    average = (one + two * 2 + three * 3 + four * 4 + five * 5) / (double) total;
                }
            }

            GetBuyListResponseDto.ProductDto.ProductDtoBuilder dtoBuilder = GetBuyListResponseDto.ProductDto.builder()
                    .buyId(buyId)
                    .productName(result.get(1, String.class))
                    .productImg(result.get(2, String.class))
                    .productPrice(result.get(3, Integer.class))
                    .discountPrice(result.get(4, Integer.class))
                    .discountPercent(result.get(5, Integer.class))
                    .skeleton(result.get(6, Integer.class))
                    .nowCount(result.get(7, Integer.class))
                    .expireDate(calculateExpireDate(result.get(8, LocalDate.class)))
                    .isLiked(result.get(9, Boolean.class))
                    .rating(average)
                    .reviewCount(result.get(10, Long.class));

            products.add(dtoBuilder.build());
            count++;
        }

        Long nextCursor = results.size() > limit ? results.get(limit).get(buy.buyId) : null;

        return GetBuyListResponseDto.builder()
                .products(products)
                .cursor(nextCursor)
                .build();
    }

    private int calculateExpireDate(LocalDate deadline) {
        if (deadline.isBefore(LocalDate.now())) {
            return -1;
        } else if (deadline.isEqual(LocalDate.now())) {
            return 0;
        } else {
            return (int) ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        }
    }

}