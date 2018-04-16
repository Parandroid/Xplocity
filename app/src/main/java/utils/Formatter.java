package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dmitry on 09.09.17.
 */

public class Formatter {

    // Convert distance to kilometers and display it with formatting
    public static String formatDistance(int distance) {
        double distanceKm = distance/1000f;

        if (distanceKm < 100) {
            return String.format("%.2f", distanceKm);
        }
        else if (distanceKm < 1000) {
            return String.format("%.1f", distanceKm);
        }
        else {
            return String.format("%.0f", distanceKm);
        }
    }

    public static String formatDate(Date date) {
        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd.MM.yyyy");
        return simpleDate.format(date);
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
        return (totalMinutes / 60) + " h " + minutes + " m";
    }


    //convert from m/sec to km/h
    public static String formatSpeed(float speed) {
        return String.format("%.2f km/h", speed*3.6);
    }
}
