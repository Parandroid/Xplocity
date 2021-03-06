package utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Created by dmitry on 09.09.17.
 */

public class Formatter {

    // Convert distance to kilometers and display it with formatting
    public static String formatDistance(int distance) {
        double distanceKm = distance/1000f;

        if (distanceKm < 10) {
            return String.format("%.1f", distanceKm);
        }
        else if (distanceKm < 100) {
            return String.format("%.1f", distanceKm);
        }
        else {
            return String.format("%.0f", distanceKm);
        }
    }


    public String formatDateTimeToUTCString(DateTime date) {
        DateTimeFormatter dtfOut = ISODateTimeFormat.dateTime();
        return dtfOut.print(date);
    }

    public DateTime formatStringToUTCDatetime(String str) {
        DateTimeFormatter dtfOut = ISODateTimeFormat.dateTime();
        return dtfOut.parseDateTime(str);
    }

    public String formatDate(DateTime date) {
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("dd.MM.yyyy");
        return dtfOut.print(date);
    }

    public String formatDateToString(DateTime date) {
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MMM dd");
        return dtfOut.print(date);
    }


    public int formatDateToYear(DateTime date) {
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("yyyy");
        return Integer.valueOf(dtfOut.print(date));
    }



    //format milliseconds to hh:mm:ss format
    public static String formatDuration(int durationMs) {
        int durationSec = durationMs/1000;

        int hours = durationSec / 3600;
        int minutes = (durationSec % 3600) / 60;
        int seconds = durationSec % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    //format minutes to "5 h 20 m" format
    public static String formatHoursAndMinutes(final int totalMinutes) {
        String minutes = Integer.toString(totalMinutes % 60);
        minutes = minutes.length() == 1 ? "0" + minutes : minutes;
        return (totalMinutes / 60) + " " + ResourceGetter.getString("hour_short") + " " + minutes + " " + ResourceGetter.getString("minute_short");
    }

    //format minutes to "5.2" format
    public String formatHours(int totalMinutes) {
        return String.format("%.1f", (float) totalMinutes / 60);
    }


    //convert from m/sec to km/h
    public static String formatSpeed(float speed) {
        return formatDistance(Math.round(speed*3600));
    }


    public static int dpToPx(int dp) {
        float density = ResourceGetter.getResources().getDisplayMetrics().density;
        return (int)(dp * density);
    }
}
