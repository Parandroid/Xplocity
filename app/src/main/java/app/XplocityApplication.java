package app;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;

/**
 * Created by dmitry on 09.08.17.
 */

public class XplocityApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        XplocityApplication.context = getApplicationContext();

        //подключаем карту с выбором позиции к эмулятору
        Stetho.initializeWithDefaults(this);
    }

    public static Context getAppContext() {
        return XplocityApplication.context;
    }
}
