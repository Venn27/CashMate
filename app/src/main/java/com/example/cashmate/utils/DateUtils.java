package com.example.cashmate.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final Locale locale = new Locale("id", "ID");

    public static String formatDate(Date date, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern, locale);
        return dateFormat.format(date);
    }

    /**
     * Get date range based on period type
     * @param periodType 0 = daily, 1 = weekly, 2 = monthly, 3 = yearly
     * @return long[] array with startDate and endDate in milliseconds
     */
    public static long[] getDateRangeForPeriod(int periodType) {
        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();
        long startDate;

        switch (periodType) {
            case 0: // Daily
                // Last 7 days
                calendar.setTime(new Date(now));
                calendar.add(Calendar.DAY_OF_MONTH, -7);
                startDate = calendar.getTimeInMillis();
                break;

            case 1: // Weekly
                // Last 4 weeks
                calendar.setTime(new Date(now));
                calendar.add(Calendar.WEEK_OF_YEAR, -4);
                startDate = calendar.getTimeInMillis();
                break;

            case 2: // Monthly
                // Last 6 months
                calendar.setTime(new Date(now));
                calendar.add(Calendar.MONTH, -6);
                startDate = calendar.getTimeInMillis();
                break;

            case 3: // Yearly
                // Last 2 years
                calendar.setTime(new Date(now));
                calendar.add(Calendar.YEAR, -2);
                startDate = calendar.getTimeInMillis();
                break;

            default:
                // Default to last month
                calendar.setTime(new Date(now));
                calendar.add(Calendar.MONTH, -1);
                startDate = calendar.getTimeInMillis();
        }

        return new long[]{startDate, now};
    }

    /**
     * Get number of data points to display for each period type
     * @param periodType 0 = daily, 1 = weekly, 2 = monthly, 3 = yearly
     * @return number of data points
     */
    public static int getNumberOfPointsForPeriod(int periodType) {
        switch (periodType) {
            case 0: // Daily
                return 7;  // 7 days
            case 1: // Weekly
                return 4;  // 4 weeks
            case 2: // Monthly
                return 6;  // 6 months
            case 3: // Yearly
                return 2;  // 2 years
            default:
                return 7;
        }
    }

    /**
     * Get date range for a specific data point
     * @param periodType 0 = daily, 1 = weekly, 2 = monthly, 3 = yearly
     * @param pointIndex index of the point (0 = oldest, n-1 = newest)
     * @return long[] array with startDate and endDate in milliseconds
     */
    public static long[] getDateRangeForPoint(int periodType, int pointIndex) {
        int numPoints = getNumberOfPointsForPeriod(periodType);
        long now = System.currentTimeMillis();
        Calendar calendar = Calendar.getInstance();

        // Set base to current time
        calendar.setTime(new Date(now));

        // Calculate position from current time
        int position = numPoints - 1 - pointIndex;

        switch (periodType) {
            case 0: // Daily
                // Each point is one day
                calendar.add(Calendar.DAY_OF_MONTH, -position);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long startOfDay = calendar.getTimeInMillis();

                calendar.add(Calendar.DAY_OF_MONTH, 1);
                long endOfDay = calendar.getTimeInMillis() - 1;

                return new long[]{startOfDay, endOfDay};

            case 1: // Weekly
                // Each point is one week
                calendar.add(Calendar.WEEK_OF_YEAR, -position);
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long startOfWeek = calendar.getTimeInMillis();

                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                long endOfWeek = calendar.getTimeInMillis() - 1;

                return new long[]{startOfWeek, endOfWeek};

            case 2: // Monthly
                // Each point is one month
                calendar.add(Calendar.MONTH, -position);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long startOfMonth = calendar.getTimeInMillis();

                calendar.add(Calendar.MONTH, 1);
                long endOfMonth = calendar.getTimeInMillis() - 1;

                return new long[]{startOfMonth, endOfMonth};

            case 3: // Yearly
                // Each point is one year
                calendar.add(Calendar.YEAR, -position);
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                long startOfYear = calendar.getTimeInMillis();

                calendar.add(Calendar.YEAR, 1);
                long endOfYear = calendar.getTimeInMillis() - 1;

                return new long[]{startOfYear, endOfYear};

            default:
                return new long[]{0, now};
        }
    }

    /**
     * Get label for a specific data point
     * @param periodType 0 = daily, 1 = weekly, 2 = monthly, 3 = yearly
     * @param pointIndex index of the point
     * @return String label
     */
    public static String getLabelForPoint(int periodType, int pointIndex) {
        long[] dateRange = getDateRangeForPoint(periodType, pointIndex);
        Date date = new Date(dateRange[0]);

        switch (periodType) {
            case 0: // Daily
                return formatDate(date, "dd/MM");
            case 1: // Weekly
                return formatDate(date, "dd/MM");
            case 2: // Monthly
                return formatDate(date, "MMM");
            case 3: // Yearly
                return formatDate(date, "yyyy");
            default:
                return formatDate(date, "dd/MM/yyyy");
        }
    }
}