package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dmitry on 09.09.17.
 */

public class Formatter {
    public static String formatDistance(int distance) {
        String result = String.format("%.2f", (double)distance/1000) + " km";
        return result;
    }

    public static String formatDate(Date date) {
        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return simpleDate.format(date);
    }

    public static String formatDuration(int duration) {
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


    //convert from m/sec to km/h
    public static String formatSpeed(float speed) {
        return String.format("%.2f km/h", speed*3.6);
    }
}
