package utils.Log;

import android.util.Log;

public class Logger {
    public enum LogLevel
    {
        VERBOSE,
        DEBUG,
        INFO,
        WARNING,
        ERROR,
        FATAL_ERROR,
    }

    private final String m_log_tag;
    private LogLevel m_log_level = LogLevel.INFO;

    public Logger(final String logging_tag, final LogLevel level) throws IllegalArgumentException{
        if(logging_tag == null) {
            throw new IllegalArgumentException("Log tag is null");
        }

        if(level == null) {
            throw new IllegalArgumentException("Log level is null");
        }

        m_log_tag = logging_tag;
        m_log_level = level;
    }

    public Logger(final String logging_tag) throws IllegalArgumentException{
        this(logging_tag, LogLevel.INFO);
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

        if(level.ordinal() >= m_log_level.ordinal()) {
            switch(level) {
                case VERBOSE:
                    if(exception==null) {
                        Log.v(m_log_tag, message);
                    }
                    else{
                        Log.v(m_log_tag,message,exception);
                    }

                    break;
                case DEBUG:
                    if(exception==null) {
                        Log.d(m_log_tag, message);
                    }
                    else{
                        Log.d(m_log_tag,message,exception);
                    }

                    break;
                case INFO:
                    if(exception==null) {
                        Log.i(m_log_tag, message);
                    }
                    else{
                        Log.i(m_log_tag,message,exception);
                    }

                    break;
                case WARNING:
                    if(exception==null) {
                        Log.w(m_log_tag, message);
                    }
                    else {
                        Log.w(m_log_tag, message, exception);
                    }

                    break;
                case ERROR:
                    if(exception==null) {
                        Log.e(m_log_tag, message);
                    }
                    else {
                        Log.e(m_log_tag, message, exception);
                    }

                    break;
                case FATAL_ERROR:
                    if(exception==null) {
                        Log.wtf(m_log_tag, message);
                    }
                    else {
                        Log.wtf(m_log_tag, message, exception);
                    }

                    break;
            }

        }
    }

    public synchronized LogLevel getLogLevel(){
        return m_log_level;
    }

    public synchronized void setLogLevel(final LogLevel level) throws IllegalArgumentException{
        if(level == null) {
            throw new IllegalArgumentException("Log level is null");
        }

        m_log_level = level;
    }
}


