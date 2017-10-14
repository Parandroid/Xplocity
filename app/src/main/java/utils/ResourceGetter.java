package utils;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;

import app.XplocityApplication;

/**
 * Created by skoparov on 8/23/17.
 */

public class ResourceGetter {
    private static String mPackageName;

    public static String getString(final String name) {
        Resources resources = XplocityApplication.getAppContext().getResources();
        return resources.getString(resources.getIdentifier(name, "string", getPackageName()));
    }

    public static int getInteger(final String name) {
        Resources resources = XplocityApplication.getAppContext().getResources();
        return resources.getInteger(resources.getIdentifier(name, "integer", getPackageName()));
    }

    public static Resources getResources() {
        Resources resources = XplocityApplication.getAppContext().getResources();
        return resources;
    }


    private static String getPackageName() {
        if( mPackageName == null ) {
            mPackageName = XplocityApplication.getAppContext().getPackageName();
        }

        return mPackageName;
    }
}
