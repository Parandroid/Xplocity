package utils.Factory;

import utils.Log.LogLevel;
import utils.Log.Logger;
import utils.ResourceGetter;

public final class LogFactory {
    public static<T> Logger createLogger(final T owner, final LogLevel level) throws IllegalArgumentException {
        return new Logger(ResourceGetter.getString("app_name") + ":" + owner.getClass().getSimpleName(), level);
    }
}
