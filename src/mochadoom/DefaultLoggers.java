/*
 * Copyright (C) 2017 Good Sign
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mochadoom;

import awt.DoomWindow;
import awt.EventBase;
import awt.EventBase.ActionMode;
import awt.EventBase.ActionStateHolder;
import awt.EventBase.RelationType;
import doom.CVarManager;
import doom.ConfigManager;
import doom.DoomMain;
import i.DoomSystem;
import p.ActiveStates;
import v.graphics.Patches;

import java.awt.*;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Facility to manage Logger Levels for different classes
 * All of that should be used instead of System.err.println for debug
 *
 * @author Good Sign
 */
public class DefaultLoggers {

    private static final Level DEFAULT_LEVEL = Level.WARNING;

    private static final Map<Level, java.util.logging.Logger> PARENT_LOGGERS_MAP = Stream.of(
            Level.FINE, Level.FINER, Level.FINEST, Level.INFO, Level.SEVERE, Level.WARNING
    ).collect(Collectors.toMap(l -> l, DefaultLoggers::newLoggerHandlingLevel));

    private static final java.util.logging.Logger DEFAULT_LOGGER = PARENT_LOGGERS_MAP.get(DEFAULT_LEVEL);
    private static final HashMap<String, java.util.logging.Logger> INDIVIDUAL_CLASS_LOGGERS = new HashMap<>();

    static {
        //INDIVIDUAL_CLASS_LOGGERS.put(EventObserver.class.getName(), PARENT_LOGGERS_MAP.get(Level.FINE));
        //INDIVIDUAL_CLASS_LOGGERS.put(TraitFactory.class.getName(), PARENT_LOGGERS_MAP.get(Level.FINER));
        INDIVIDUAL_CLASS_LOGGERS.put(ActiveStates.class.getName(), PARENT_LOGGERS_MAP.get(Level.FINER));
        INDIVIDUAL_CLASS_LOGGERS.put(DoomWindow.class.getName(), PARENT_LOGGERS_MAP.get(Level.FINE));
        INDIVIDUAL_CLASS_LOGGERS.put(Patches.class.getName(), PARENT_LOGGERS_MAP.get(Level.INFO));
        INDIVIDUAL_CLASS_LOGGERS.put(ConfigManager.class.getName(), PARENT_LOGGERS_MAP.get(Level.INFO));
        INDIVIDUAL_CLASS_LOGGERS.put(DoomMain.class.getName(), PARENT_LOGGERS_MAP.get(Level.INFO));
        INDIVIDUAL_CLASS_LOGGERS.put(DoomSystem.class.getName(), PARENT_LOGGERS_MAP.get(Level.INFO));
        INDIVIDUAL_CLASS_LOGGERS.put(CVarManager.class.getName(), PARENT_LOGGERS_MAP.get(Level.INFO));
        INDIVIDUAL_CLASS_LOGGERS.put(Engine.class.getName(), PARENT_LOGGERS_MAP.get(Level.INFO));
    }

    public static java.util.logging.Logger getLogger(final String className) {
        final var ret = java.util.logging.Logger.getLogger(className);
        ret.setParent(INDIVIDUAL_CLASS_LOGGERS.getOrDefault(className, DEFAULT_LOGGER));

        return ret;
    }

    private static EventBase<?> lastHandler = null;

    public static <EventHandler extends Enum<EventHandler> & EventBase<EventHandler>> void LogEvent(
            final Logger logger,
            final ActionStateHolder<EventHandler> actionStateHolder,
            final EventHandler handler,
            final AWTEvent event
    ) {
        if (!logger.isLoggable(Level.ALL) && lastHandler == handler) {
            return;
        }

        lastHandler = handler;

        @SuppressWarnings("unchecked")
        final IntFunction<EventBase<EventHandler>[]> arrayGenerator = EventBase[]::new;
        final EventBase<EventHandler>[] depends = actionStateHolder
                .cooperations(handler, RelationType.DEPEND)
                .stream()
                .filter(hdl -> actionStateHolder.hasActionsEnabled(hdl, ActionMode.DEPEND))
                .toArray(arrayGenerator);

        final Map<RelationType, Set<EventHandler>> adjusts = actionStateHolder
                .adjustments(handler);

        final EventBase<EventHandler>[] causes = actionStateHolder
                .cooperations(handler, RelationType.CAUSE)
                .stream()
                .filter(hdl -> actionStateHolder.hasActionsEnabled(hdl, ActionMode.DEPEND))
                .toArray(arrayGenerator);

        final EventBase<EventHandler>[] reverts = actionStateHolder
                .cooperations(handler, RelationType.REVERT)
                .stream()
                .filter(hdl -> actionStateHolder.hasActionsEnabled(hdl, ActionMode.DEPEND))
                .toArray(arrayGenerator);

        if (logger.isLoggable(Level.FINEST)) {
            logger.log(Level.FINEST, () -> String.format(
                    "\n\nENCOUNTERED EVENT: %s [%s] \n%s: %s \n%s \n%s: %s \n%s: %s \nOn event: %s",
                    handler, ActionMode.PERFORM,
                    RelationType.DEPEND, Arrays.toString(depends),
                    adjusts.entrySet().stream().collect(StringBuilder::new, (sb, e) -> sb.append(e.getKey()).append(' ').append(e.getValue()).append('\n'), StringBuilder::append),
                    RelationType.CAUSE, Arrays.toString(causes),
                    RelationType.REVERT, Arrays.toString(reverts),
                    event
            ));
        } else if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, () -> String.format(
                    "\n\nENCOUNTERED EVENT: %s [%s] \n%s: %s \n%s \n%s: %s \n%s: %s \n",
                    handler, ActionMode.PERFORM,
                    RelationType.DEPEND, Arrays.toString(depends),
                    adjusts.entrySet().stream().collect(StringBuilder::new, (sb, e) -> sb.append(e.getKey()).append(' ').append(e.getValue()).append('\n'), StringBuilder::append),
                    RelationType.CAUSE, Arrays.toString(causes),
                    RelationType.REVERT, Arrays.toString(reverts)
            ));
        } else {
            logger.log(Level.FINE, () -> String.format(
                    "\nENCOUNTERED EVENT: %s [%s]",
                    handler, ActionMode.PERFORM
            ));
        }
    }

    private DefaultLoggers() {
    }

    private static java.util.logging.Logger newLoggerHandlingLevel(final Level l) {
        final OutHandler h = new OutHandler();
        h.setLevel(l);
        final var ret = java.util.logging.Logger.getAnonymousLogger();
        ret.setUseParentHandlers(false);
        ret.setLevel(l);
        ret.addHandler(h);
        return ret;
    }

    public static Logger getLoggerWrapped(String className) {
        return new WrappedLogger(getLogger(className));
    }

    private static final class OutHandler extends ConsoleHandler {

        @Override
        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        protected synchronized void setOutputStream(final OutputStream out) throws SecurityException {
            super.setOutputStream(System.out);
        }
    }

    public record WrappedLogger(java.util.logging.Logger logger) implements Logger {
        @Override
        public boolean isLoggable(Level level) {
            return logger.isLoggable(level);
        }

        @Override
        public void log(Level level, String text) {
            logger.log(level, text);
        }

        @Override
        public void log(Level level, Supplier<String> text) {
            logger.log(level, text);
        }

        @Override
        public void log(Level level, String text, Throwable e) {
            logger.log(level, text, e);
        }

        @Override
        public void log(Level level, String text, Object... objects) {
            logger.log(level, text, objects);
        }

        @Override
        public void log(Level level, Throwable e, Supplier<String> text) {
            logger.log(level, e, text);
        }
    }
}