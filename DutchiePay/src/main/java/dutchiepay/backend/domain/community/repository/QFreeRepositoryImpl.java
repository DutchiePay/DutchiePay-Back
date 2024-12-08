package dutchiepay.backend.domain.community.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dutchiepay.backend.domain.ChronoUtil;
import dutchiepay.backend.domain.community.dto.FreeListResponseDto;
import dutchiepay.backend.domain.community.dto.HotAndRecommendsResponseDto;
import dutchiepay.backend.domain.community.exception.CommunityErrorCode;
import dutchiepay.backend.domain.community.exception.CommunityException;
import dutchiepay.backend.entity.Free;
import dutchiepay.backend.entity.QComment;
import dutchiepay.backend.entity.QFree;
import dutchiepay.backend.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class QFreeRepositoryImpl implements QFreeRepository {

    private final JPAQueryFactory jpaQueryFactory;

    QUser user = QUser.user;
    QFree free = QFree.free;
    QComment comment = QComment.comment;

    @Override
    public FreeListResponseDto getFreeLists(String category, String filter, int limit, Long cursor) {
        // 커서 초기화
        if (cursor == null) cursor = Long.MAX_VALUE;

        // 기본 select, from 문에 조건 추가
        JPAQuery<Free> query = jpaQueryFactory
                .selectFrom(free)
                .leftJoin(comment).on(comment.free.eq(free))
                .groupBy(free);

        // category가 있으면 검색 조건에 category 추가
        if (StringUtils.hasText(category)) {
            query.where(free.category.eq(category));
        }
        OrderSpecifier[] orderSpecifier;
        switch (filter) {
            case "new":
                orderSpecifier = new OrderSpecifier[]{free.createdAt.desc()};
                query.where(free.freeId.loe(cursor));
                break;
            case "comment":
                orderSpecifier = new OrderSpecifier[]{comment.count().desc(), free.freeId.desc()};
                if (cursor < Long.MAX_VALUE) {
                    Long nowCommentsCount = jpaQueryFactory.
                            select(comment.count())
                            .from(comment)
                            .join(comment.free, free)
                            .where(free.freeId.eq(cursor))
                            .fetchFirst();
                    if (nowCommentsCount != null) {
                        query.having(comment.count().lt(nowCommentsCount)
                                .or(comment.count().eq(nowCommentsCount))
                                .and(free.freeId.loe(cursor)));
                    }
                }
                break;
            case "view":
                orderSpecifier = new OrderSpecifier[]{free.hits.desc(), free.freeId.desc()};
                if (cursor < Long.MAX_VALUE) {
                    Integer nowViews = jpaQueryFactory
                            .select(free.hits)
                            .from(free)
                            .where(free.freeId.eq(cursor))
                            .fetchFirst();
                    if (nowViews != null) {
                        query.where(free.hits.lt(nowViews)
                                .or(free.hits.eq(nowViews)).and(free.freeId.ne(cursor)));
                    }
                }
                break;
            default: throw new CommunityException(CommunityErrorCode.ILLEGAL_FILTER);
        }

        query.limit(limit + 1).orderBy(orderSpecifier);
        List<Free> posts = query.fetch();
        for (Free free1 : posts) {
            System.out.println("=========Query Result=========");
            System.out.println(free1.getFreeId());
            System.out.println(free1.getUser().getNickname());
            System.out.println(free1.getTitle());
            System.out.println(free1.getContents());
            System.out.println(free1.getCategory());
            System.out.println(free1.getCreatedAt());
            System.out.println(free1.getHits());
        }

        Long nextCursor = posts.size() > limit ? posts.get(limit).getFreeId() : null;

        return FreeListResponseDto.toDto(posts.stream().map(free ->
                        FreeListResponseDto.FreeList.toDto(free, ChronoUtil.timesAgo(free.getCreatedAt()), countComments(free)))
                .collect(Collectors.toList()), nextCursor);
    }

    @Override
    public Tuple getFreePost(Long freeId) {
        return jpaQueryFactory
                .select(
                        user.userId,
                        user.nickname,
                        user.profileImg,
                        free.title,
                        free.contents,
                        free.createdAt,
                        free.category,
                        free.hits,
                        Expressions.as(
                                JPAExpressions
                                        .select(comment.count())
                                        .from(comment)
                                        .where(comment.free.freeId.eq(freeId)),
                                "commentsCount"
                        )
                )
                .from(free)
                .join(free.user, user)
                .where(free.freeId.eq(freeId))
                .fetchFirst();
    }

    @Override
    public List<HotAndRecommendsResponseDto.Posts> getHotPosts() {

        return jpaQueryFactory
                .select(free)
                .where(free.createdAt.goe(LocalDateTime.now().minusDays(7)))
                .orderBy(free.hits.desc())
                .limit(5)
                .fetch()
                .stream()
                .map(free -> HotAndRecommendsResponseDto.Posts.toDto(free, countComments(free)))
                .toList();
    }

    @Override
    public List<HotAndRecommendsResponseDto.Posts> getRecommendsPosts(String category) {
        return jpaQueryFactory
                .selectFrom(free)
                .where(free.category.eq(category))
                .orderBy(free.createdAt.desc())
                .limit(5)
                .fetch()
                .stream()
                .map(free -> HotAndRecommendsResponseDto.Posts.toDto(free, countComments(free)))
                .toList();
    }

    private Long countComments(Free free) {
        return jpaQueryFactory
                .select(comment.count())
                .from(comment)
                .where(comment.free.eq(free))
                .fetchFirst();
    }
}
