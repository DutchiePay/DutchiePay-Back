package dutchiepay.backend.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

//LocalDateTime -> 몇분 전, 몇시간 전, 며칠 전으로 출력하는 Util
public class ChronoUtil {
    public static String timesAgo(LocalDateTime dayBefore) {
        long gap = ChronoUnit.MINUTES.between(dayBefore, LocalDateTime.now());
        String word;
        if (gap == 0){
            word = "방금 전";
        }else if (gap < 60) {
            word = gap + "분 전";
        }else if (gap < 60 * 24){
            word = (gap/60) + "시간 전";
        }else if (gap < 60 * 24 * 7) {
            word = (gap/60/24) + "일 전";
        }else if (gap < 60 * 24 * 30) {
            word = (gap/60/24/7) + "주 전";
        }else if (gap < 60 * 24 * 365) {
            word = (gap/60/24/30) + "달 전";
        }else {
            word = (gap/60/24/365) + "년 전";
        }
        return word;
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        LocalDateTime now = LocalDateTime.now();
        String word;
        if (dateTime == null) {
            return null;
        }

        if (dateTime.toLocalDate().equals(now.toLocalDate())) { //당일이면 오후 2:30
            word = dateTime.format(DateTimeFormatter.ofPattern("a h:mm").withLocale(Locale.forLanguageTag("ko")));
        } else { //당일이 아니면 2월 11일
            word = dateTime.format(DateTimeFormatter.ofPattern("MM월 dd일"));
        }
        return word;
    }
}