package utils.Log;

import android.util.Log;

public class Logger {
    private final String mLogTag;
    private LogLevel mLogLevel = LogLevel.INFO;

    public Logger(final String logTag, final LogLevel level) throws IllegalArgumentException{
        if(logTag == null) {
            throw new IllegalArgumentException("Log tag is null");
        }

        if(level == null) {
            throw new IllegalArgumentException("Log level is null");
        }

        mLogTag = logTag;
        mLogLevel = level;
    }

    public void logVerbose(final String message) throws IllegalArgumentException{
        addLogEntry(message, LogLevel.VERBOSE, null);
    }

    public void logVerbose(final String message, final Throwable exception) throws IllegalArgumentException{
        addLogEntry(message, LogLevel.VERBOSE, exception);
    }

    public void logDebug(final String message) throws IllegalArgumentException{
        addLogEntry(message, LogLevel.DEBUG, null);
    }

    public void logDebug(final String message, final Throwable exception) throws IllegalArgumentException{
        addLogEntry(message, LogLevel.DEBUG, exception);
    }

    public void logInfo(final String message) throws IllegalArgumentException{
        addLogEntry(message, LogLevel.INFO, null);
    }

    public void logInfo(final String message, final Throwable exception) throws IllegalArgumentException{
        addLogEntry(message, LogLevel.INFO, exception);
    }

    public void logWarning(final String message) throws IllegalArgumentException{
        addLogEntry(message, LogLevel.WARNING, null);
    }

    public void logWarning(final String message, final Throwable exception) throws IllegalArgumentException{
        addLogEntry(message, LogLevel.WARNING, exception);
    }

    public void logError(final String message, final Throwable exception) throws IllegalArgumentException{
        addLogEntry(message, LogLevel.ERROR, exception);
    }

    public void logError(final String message) throws IllegalArgumentException{
        addLogEntry(message, LogLevel.ERROR, null);
    }

    public void logFatal(final String message) throws IllegalArgumentException{
        addLogEntry(message, LogLevel.FATAL_ERROR, null);
    }

    public void logFatal(final String message, final Throwable exception) throws IllegalArgumentException{
        addLogEntry(message, LogLevel.FATAL_ERROR, exception);
    }

    private synchronized void addLogEntry(final String message, final LogLevel level, final Throwable exception) throws IllegalArgumentException{
        if(message == null) {
            throw new IllegalArgumentException("Log message is null");
        }

        if(level.ordinal() >= mLogLevel.ordinal()) {
            switch(level) {
                case VERBOSE:
                    if(exception==null) {
                        Log.v(mLogTag, message);
                    }
                    else{
                        Log.v(mLogTag,message,exception);
                    }

                    break;
                case DEBUG:
                    if(exception==null) {
                        Log.d(mLogTag, message);
                    }
                    else{
                        Log.d(mLogTag,message,exception);
                    }

                    break;
                case INFO:
                    if(exception==null) {
                        Log.i(mLogTag, message);
                    }
                    else{
                        Log.i(mLogTag,message,exception);
                    }

                    break;
                case WARNING:
                    if(exception==null) {
                        Log.w(mLogTag, message);
                    }
                    else {
                        Log.w(mLogTag, message, exception);
                    }

                    break;
                case ERROR:
                    if(exception==null) {
                        Log.e(mLogTag, message);
                    }
                    else {
                        Log.e(mLogTag, message, exception);
                    }

                    break;
                case FATAL_ERROR:
                    if(exception==null) {
                        Log.wtf(mLogTag, message);
                    }
                    else {
                        Log.wtf(mLogTag, message, exception);
                    }

                    break;
            }

        }
    }

    public synchronized LogLevel getLogLevel(){
        return mLogLevel;
    }

    public synchronized void setLogLevel(final LogLevel level) throws IllegalArgumentException{
        if(level == null) {
            throw new IllegalArgumentException("Log level is null");
        }

        mLogLevel = level;
    }
}


