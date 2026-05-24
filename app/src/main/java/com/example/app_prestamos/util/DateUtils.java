package com.example.app_prestamos.util;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public final class DateUtils {

    private static final long MILLIS_PER_DAY = 86_400_000L;
    private static final Locale SPANISH_LOCALE = Locale.forLanguageTag("es-ES");

    private DateUtils() {
        // Clase utilitaria: no necesita estado ni instancias.
    }

    public static long todayEpochDay() {
        Calendar calendar = Calendar.getInstance();
        clearTime(calendar);
        return calendar.getTimeInMillis() / MILLIS_PER_DAY;
    }

    public static long epochDayFor(int year, int zeroBasedMonth, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, zeroBasedMonth);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        clearTime(calendar);
        return calendar.getTimeInMillis() / MILLIS_PER_DAY;
    }

    public static long addFrequency(long startEpochDay, String paymentFrequency, int periods) {
        Calendar calendar = epochDayToCalendar(startEpochDay);
        if ("MONTHLY".equals(paymentFrequency)) {
            calendar.add(Calendar.MONTH, periods);
        } else if ("BIWEEKLY".equals(paymentFrequency)) {
            calendar.add(Calendar.DAY_OF_YEAR, 15 * periods);
        } else {
            calendar.add(Calendar.DAY_OF_YEAR, periods);
        }
        clearTime(calendar);
        return calendar.getTimeInMillis() / MILLIS_PER_DAY;
    }

    public static String formatEpochDay(long epochDay) {
        Calendar calendar = epochDayToCalendar(epochDay);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String month = new DateFormatSymbols(SPANISH_LOCALE).getMonths()[calendar.get(Calendar.MONTH)];
        return day + " de " + month;
    }

    public static String currentMonthTitle() {
        Calendar calendar = Calendar.getInstance();
        String month = new DateFormatSymbols(SPANISH_LOCALE).getMonths()[calendar.get(Calendar.MONTH)];
        return capitalize(month) + " " + calendar.get(Calendar.YEAR);
    }

    public static String monthTitleFor(int year, int zeroBasedMonth) {
        String month = new DateFormatSymbols(SPANISH_LOCALE).getMonths()[zeroBasedMonth];
        return capitalize(month) + " " + year;
    }

    private static Calendar epochDayToCalendar(long epochDay) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTimeInMillis(epochDay * MILLIS_PER_DAY);
        clearTime(calendar);
        return calendar;
    }

    private static void clearTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.substring(0, 1).toUpperCase(SPANISH_LOCALE) + value.substring(1);
    }
}
