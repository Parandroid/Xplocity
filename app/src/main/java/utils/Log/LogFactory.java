package utils.Log;

public final class LogFactory {
    public static Logger createLogger(final Class owner) throws IllegalArgumentException {
        return new Logger(owner.toString());
    }
}
