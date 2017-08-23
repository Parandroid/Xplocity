package utils;

import android.content.res.Resources;

import app.XplocityApplication;
import utils.Log.LogLevel;

public class LogLevelGetter
{
    private static LogLevel logLevelFromStr(final String logLevelStr) throws IllegalArgumentException{
        String lowerLogLevelString = logLevelStr.toLowerCase();
        LogLevel result;

        switch (lowerLogLevelString)
        {
            case "verbose":
                result = LogLevel.VERBOSE;
                break;
            case "debug":
                result = LogLevel.DEBUG;
                break;
            case "info":
                result = LogLevel.INFO;
                break;
            case "warning":
                result = LogLevel.WARNING;
                break;
            case "error":
                result = LogLevel.ERROR;
                break;
            case "terrible_error":
                result = LogLevel.FATAL_ERROR;
                break;
            default:
                throw new IllegalArgumentException("Invalid log level:" + logLevelStr);
        }

        return result;
    }

    public static LogLevel get() throws IllegalArgumentException {
        Resources resources = XplocityApplication.getAppContext().getResources();
        String packageName = XplocityApplication.getAppContext().getPackageName();
        return logLevelFromStr(resources.getString(resources.getIdentifier("log_level", "string", packageName)));
    }
}
