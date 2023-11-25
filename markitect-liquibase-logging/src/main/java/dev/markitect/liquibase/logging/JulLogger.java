/*
 * Copyright 2023 Markitect
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.markitect.liquibase.logging;

import static dev.markitect.liquibase.base.Preconditions.checkNotNull;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import liquibase.logging.core.AbstractLogger;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("squid:S2160")
public class JulLogger extends AbstractLogger {
  private static final StackWalker stackWalker =
      StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

  private final @Nullable String name;
  private final Logger logger;

  JulLogger(@Nullable String name) {
    this.name = name;
    this.logger = Logger.getLogger(name);
  }

  @Override
  public void close() {
    // Redeclared to throw no exception
  }

  @Override
  public void log(Level level, @Nullable String message, @Nullable Throwable e) {
    checkNotNull(level);
    if (logger.isLoggable(level)) {
      logger.log(toLogRecord(level, message, e));
    }
  }

  private LogRecord toLogRecord(Level level, @Nullable String message, @Nullable Throwable e) {
    checkNotNull(level);
    var logRecord = new LogRecord(level, message);
    logRecord.setLoggerName(name);
    logRecord.setThrown(e);
    var found = new AtomicBoolean();
    stackWalker
        .walk(
            stream ->
                stream
                    .filter(
                        stackFrame -> {
                          if (liquibase.logging.Logger.class.isAssignableFrom(
                              stackFrame.getDeclaringClass())) {
                            found.set(true);
                            return false;
                          }
                          return found.get();
                        })
                    .findFirst())
        .ifPresent(
            stackFrame -> {
              logRecord.setSourceClassName(stackFrame.getClassName());
              logRecord.setSourceMethodName(stackFrame.getMethodName());
            });
    logRecord.setResourceBundleName(logger.getResourceBundleName());
    logRecord.setResourceBundle(logger.getResourceBundle());
    return logRecord;
  }
}
