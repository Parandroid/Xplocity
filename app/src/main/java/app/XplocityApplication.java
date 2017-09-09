package app;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;

/**
 * Created by dmitry on 09.08.17.
 */

public class XplocityApplication extends Application {

    private static Context mContext;
    private static boolean mActivityVisible = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        //подключаем карту с выбором позиции к эмулятору
        Stetho.initializeWithDefaults(this);
    }

    public static Context getAppContext() {
        return mContext;
    }

    public static boolean isActivityVisible() {
        return mActivityVisible;
    }

    public static void activityResumed() {
        mActivityVisible = true;
    }

    public static void activityPaused() {
        mActivityVisible = false;
    }



}

