package utils.Log;

public final class LogLevelConverter{
        public static Logger.LogLevel logLevelFromStr(final String logLevelStr) throws IllegalArgumentException{
                String lowerLogLevelString = logLevelStr.toLowerCase();
                Logger.LogLevel result;

                switch (lowerLogLevelString)
                {
                        case "verbose":
                                result = Logger.LogLevel.VERBOSE;
                                break;
                        case "debug":
                                result = Logger.LogLevel.DEBUG;
                                break;
                        case "info":
                                result = Logger.LogLevel.INFO;
                                break;
                        case "warning":
                                result = Logger.LogLevel.WARNING;
                                break;
                        case "error":
                                result = Logger.LogLevel.ERROR;
                                break;
                        case "terrible_error":
                                result = Logger.LogLevel.FATAL_ERROR;
                                break;
                        default:
                                throw new IllegalArgumentException("Invalid log level:" + logLevelStr);
                }

                return result;
        }
}
