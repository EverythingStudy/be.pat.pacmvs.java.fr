package cn.staitech.fr.utils;


import org.springframework.data.util.Pair;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DaysOfYear {
    public static List<String> getAllDaysOfCurrentYear() {
        int year = LocalDate.now().getYear();           // 取系统时间的年份
        int length = Year.of(year).length();            // 当年天数
        return IntStream.rangeClosed(1, length)
                        .mapToObj(dayOfYear ->
                                LocalDate.ofYearDay(year, dayOfYear).toString())
                        .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<String> days = getAllDaysOfCurrentYear();
        days.forEach(System.out::println);              // 2025-01-01 … 2025-12-31
    }
}