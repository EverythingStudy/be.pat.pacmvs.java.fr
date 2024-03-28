package cn.staitech.fr.utils;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Date;


/**
 * 时间格式化
 */
public class DateUtils {
    public DateUtils() {
    }

    /**
     * 获取当前日期格式字符串
     *
     * @param pattern HH:mm:ss
     * @return
     */
    public static String getCurrentHHmmssString(String pattern) {
        return new SimpleDateFormat(pattern).format(new Date());
    }

    /**
     * 日期格式处理
     *
     * @param dateTime
     * @param n
     * @return
     */
    public static Date addAndSubtractDaysByCalendar(Date dateTime/*待处理的日期*/, int n/*加减天数*/) {
        java.util.Calendar calstart = java.util.Calendar.getInstance();
        calstart.setTime(dateTime);
        calstart.add(java.util.Calendar.DAY_OF_WEEK, n);
        return calstart.getTime();
    }

    /**
     * 使用clock.millis获取毫秒的时间戳
     *
     * @return millis 毫秒时间戳
     */
    public static Long MillisDefaultZone() {
        Clock clock = Clock.systemDefaultZone();
        long millis = clock.millis();
        return millis;
    }
}
