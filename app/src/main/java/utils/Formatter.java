package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dmitry on 09.09.17.
 */

public class Formatter {
    public static String formatDistance(int distance) {
        double distanceKm = distance/1000;

        if (distanceKm < 100) {
            return String.format("%.2f", distanceKm) + " km";
        }
        else if (distanceKm < 1000) {
            return String.format("%.1f", distanceKm) + " km";
        }
        else {
            return String.format("%.0f", distanceKm) + " km";
        }
    }

    public static String formatDate(Date date) {
        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return simpleDate.format(date);
    }

    //format milliseconds to readable duration
    public static String formatDuration(int durationMs) {
        int durationSec = durationMs/1000;

        int hours = durationSec / 3600;
        int minutes = (durationSec % 3600) / 60;
        int seconds = durationSec % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


    //convert from m/sec to km/h
    public static String formatSpeed(float speed) {
        return String.format("%.2f km/h", speed*3.6);
    }
}
