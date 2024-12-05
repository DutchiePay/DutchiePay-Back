package dutchiepay.backend.domain.community.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import dutchiepay.backend.domain.community.dto.GetMartListResponseDto;
import dutchiepay.backend.domain.community.dto.GetMartResponseDto;
import dutchiepay.backend.entity.QShare;
import dutchiepay.backend.entity.QUser;
import dutchiepay.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class QShareRepositoryImpl implements QShareRepository{

    private final JPAQueryFactory jpaQueryFactory;

    QUser user = QUser.user;
    QShare share = QShare.share;

    @Override
    public GetMartListResponseDto getMartList(User u, String category, Long cursor, Integer limit) {
        if (cursor == null) {
            cursor = Long.MAX_VALUE;
        }

        BooleanExpression condition = share.deletedAt.isNull()
                .and(share.shareId.lt(cursor));

        if (category != null && !category.isEmpty()) {
            condition = condition.and(share.category.eq(category));
        }

        if (u != null) {
            condition = condition.and(share.user.location.eq(u.getLocation()));
        } else  {
            condition = condition.and(share.user.location.eq("서울시 중구"));
        }

        List<Tuple> results = jpaQueryFactory
                .select(share.shareId,
                        share.category,
                        user.nickname,
                        share.thumbnail,
                        share.title,
                        share.meetingPlace,
                        share.state,
                        share.createdAt,
                        share.date,
                        share.maximum,
                        share.now)
                .from(share)
                .leftJoin(share.user, user)
                .where(condition)
                .orderBy(share.createdAt.desc())
                .limit(limit)
                .fetch();

        List<GetMartListResponseDto.MartDto> martList = new ArrayList<>();

        for (Tuple result : results) {
            martList.add(GetMartListResponseDto.MartDto.builder()
                    .shareId(result.get(share.shareId))
                    .category(result.get(share.category))
                    .writer(result.get(user.nickname))
                    .thumbnail(result.get(share.thumbnail))
                    .title(result.get(share.title))
                    .meetingPlace(result.get(share.meetingPlace))
                    .state(result.get(share.state))
                    .relativeTime(convertRelativeTime(result.get(share.createdAt)))
                    .date(result.get(share.date))
                    .maximum(result.get(share.maximum))
                    .now(result.get(share.now))
                    .build());
        }

        Long nextCursor = results.size() == limit ? results.get(results.size() - 1).get(share.shareId) : null;

        return GetMartListResponseDto.builder()
                .posts(martList)
                .cursor(nextCursor)
                .build();
    }

    @Override
    public GetMartResponseDto getMartByShareId(Long shareId) {
        return jpaQueryFactory
                .select(Projections.constructor(GetMartResponseDto.class,
                                user.userId.as("writerId"),
                                user.nickname.as("writer"),
                                user.profileImg.as("writerProfileImage"),
                                share.title.as("title"),
                                share.category.as("category"),
                                share.contents.as("content"),
                                share.meetingPlace.as("meetingPlace"),
                                share.longitude.as("longitude"),
                                share.latitude.as("latitude"),
                                share.state.as("state"),
                                share.createdAt.as("createdAt"),
                                share.date.as("date"),
                                share.maximum.as("maximum"),
                                share.now.as("now"),
                                share.hits.as("hits"))
                )
                .from(share)
                .leftJoin(share.user, user)
                .where(share.shareId.eq(shareId))
                .where(share.deletedAt.isNull())
                .fetchOne();
    }

    private String convertRelativeTime(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();

        long minutes = ChronoUnit.MINUTES.between(createdAt, now);
        long hours = ChronoUnit.HOURS.between(createdAt, now);
        long days = ChronoUnit.DAYS.between(createdAt, now);
        long weeks = ChronoUnit.WEEKS.between(createdAt, now);
        long months = ChronoUnit.MONTHS.between(createdAt, now);
        long years = ChronoUnit.YEARS.between(createdAt, now);

        if (minutes < 1) {
            return "방금 전";
        } else if (minutes < 60) {
            return minutes + "분 전";
        } else if (hours < 24) {
            return hours + "시간 전";
        } else if (days < 7) {
            return days + "일 전";
        } else if (days < 30) {
            return weeks + "주 전";
        } else if (days < 365) {
            return months + "달 전";
        } else {
            return years + "년 전";
        }
    }
}