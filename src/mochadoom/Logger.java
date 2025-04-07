package mochadoom;

import java.util.function.Supplier;
import java.util.logging.Level;

public interface Logger {
    Logger NOOP = new Logger() {
        @Override
        public boolean isLoggable(Level level) {
            return false;
        }

        @Override
        public void log(Level level, String text) {}
        @Override
        public void log(Level level, Supplier<String> text) {}
        @Override
        public void log(Level level, String text, Throwable e) {}
        @Override
        public void log(Level level, String text, Object... objects) {}
        @Override
        public void log(Level warning, Throwable e, Supplier<String> text) {}
    };

    static Logger getLogger(final String className) {
        if (SystemHandler.instance == null) {
            return new LazyLogger(className);
        }
        return SystemHandler.instance.getLogger(className);
    }

    boolean isLoggable(Level level);
    void log(Level level, String text);
    void log(Level level, Supplier<String> text);
    void log(Level level, String text, Throwable e);
    void log(Level level, String text, Object... objects);
    void log(Level warning, Throwable e, Supplier<String> text);


    class LazyLogger implements Logger {
        private final String className;
        private Logger logger;

        public LazyLogger(String className) {
            this.className = className;
        }

        private Logger getLogger() {
            if (logger == null) {
                logger = SystemHandler.instance.getLogger(className);
            }
            return logger;
        }

        @Override
        public boolean isLoggable(Level level) {
            return getLogger().isLoggable(level);
        }

        @Override
        public void log(Level level, String text) {
            getLogger().log(level, text);
        }

        @Override
        public void log(Level level, Supplier<String> text) {
            getLogger().log(level, text);
        }

        @Override
        public void log(Level level, String text, Throwable e) {
            getLogger().log(level, text, e);
        }

        @Override
        public void log(Level level, String text, Object... objects) {
            getLogger().log(level, text, objects);
        }

        @Override
        public void log(Level level, Throwable e, Supplier<String> text) {
            getLogger().log(level, e, text);
        }
    }
}
