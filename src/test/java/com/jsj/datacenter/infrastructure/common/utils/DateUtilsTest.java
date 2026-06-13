package com.jsj.datacenter.infrastructure.common.utils;

import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void getNowDate_returnsCurrentDate() {
        Date now = DateUtils.getNowDate();
        assertNotNull(now);
        assertTrue(Math.abs(now.getTime() - System.currentTimeMillis()) < 2000);
    }

    @Test
    void getDate_returnsCorrectFormat() {
        String date = DateUtils.getDate();
        assertEquals(10, date.length()); // yyyy-MM-dd
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);
        try {
            sdf.parse(date); // won't throw if format is valid
        } catch (ParseException e) {
            fail("Unexpected parse exception: " + e.getMessage());
        }
    }

    @Test
    void getTime_returnsCorrectFormat() {
        String time = DateUtils.getTime();
        assertEquals(19, time.length()); // yyyy-MM-dd HH:mm:ss
    }

    @Test
    void dateTimeNow_withFormat() {
        String result = DateUtils.dateTimeNow("yyyyMMdd");
        assertEquals(8, result.length());
    }

    @Test
    void dateTimeWithDate() {
        Date date = new Date();
        String result = DateUtils.dateTime(date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals(sdf.format(date), result);
    }

    @Test
    void parseDateToStr_formatsCorrectly() {
        Date date = new Date();
        String result = DateUtils.parseDateToStr("yyyy-MM-dd HH:mm", date);
        assertEquals(16, result.length());
    }

    @Test
    void dateTime_withValidString_parsesCorrectly() {
        Date result = DateUtils.dateTime("yyyy-MM-dd", "2025-06-11");
        assertNotNull(result);
    }

    @Test
    void dateTime_withInvalidString_throwsException() {
        assertThrows(RuntimeException.class, () -> {
            DateUtils.dateTime("yyyy-MM-dd", "not-a-date");
        });
    }

    @Test
    void datePath_returnsCorrectFormat() {
        String path = DateUtils.datePath();
        assertTrue(path.matches("\\d{4}/\\d{2}/\\d{2}"));
    }

    @Test
    void dateTime_returnsDateAsYyyymmdd() {
        String result = DateUtils.dateTime();
        assertEquals(8, result.length());
    }

    @Test
    void parseDate_withNull_returnsNull() {
        assertNull(DateUtils.parseDate(null));
    }

    @Test
    void parseDate_withValidString() throws ParseException {
        Date result = DateUtils.parseDate("2025-06-11");
        assertNotNull(result);
    }

    @Test
    void parseDate_withValidDateTimeString() throws ParseException {
        Date result = DateUtils.parseDate("2025-06-11 14:30:00");
        assertNotNull(result);
    }

    @Test
    void parseDate_withInvalidString_returnsNull() {
        assertNull(DateUtils.parseDate("not-a-date"));
    }

    @Test
    void parseDate_withSlashFormat() throws ParseException {
        Date result = DateUtils.parseDate("2025/06/11");
        assertNotNull(result);
    }

    @Test
    void parseDate_withDotFormat() throws ParseException {
        Date result = DateUtils.parseDate("2025.06.11");
        assertNotNull(result);
    }

    @Test
    void differentDaysByMillisecond_sameDay_returnsZero() {
        Date d1 = new Date();
        Date d2 = new Date(System.currentTimeMillis() + 3600000); // 1 hour later
        assertEquals(0, DateUtils.differentDaysByMillisecond(d1, d2));
    }

    @Test
    void differentDaysByMillisecond_differentDays() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date d1 = sdf.parse("2025-06-10");
            Date d2 = sdf.parse("2025-06-13");
            assertEquals(3, DateUtils.differentDaysByMillisecond(d1, d2));
        } catch (ParseException e) {
            fail("Unexpected parse exception");
        }
    }

    @Test
    void timeDistance_returnsCorrectFormat() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date start = sdf.parse("2025-06-11 00:00:00");
            Date end = sdf.parse("2025-06-11 02:30:00");
            String result = DateUtils.timeDistance(end, start);
            assertTrue(result.contains("天"));
            assertTrue(result.contains("小时"));
            assertTrue(result.contains("分钟"));
        } catch (ParseException e) {
            fail("Unexpected parse exception");
        }
    }

    @Test
    void toHumanTime_withZero() {
        String result = DateUtils.toHumanTime(0);
        assertEquals("0天0小时0分钟0秒", result);
    }

    @Test
    void toHumanTime_withMixedUnits() {
        long ms = 24 * 60 * 60 * 1000L + 3600 * 1000L + 30 * 60 * 1000L + 15 * 1000L;
        String result = DateUtils.toHumanTime(ms);
        assertEquals("1天1小时30分钟15秒", result);
    }

    @Test
    void toDate_withLocalDateTime() {
        LocalDateTime ldt = LocalDateTime.of(2025, 6, 11, 12, 0, 0);
        Date result = DateUtils.toDate(ldt);
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        assertEquals("2025-06-11 12:00:00", sdf.format(result));
    }

    @Test
    void toDate_withLocalDate() {
        LocalDate ld = LocalDate.of(2025, 6, 11);
        Date result = DateUtils.toDate(ld);
        assertNotNull(result);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals("2025-06-11", sdf.format(result));
    }
}
