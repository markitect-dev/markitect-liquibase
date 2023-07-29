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

import dev.markitect.liquibase.base.Nullable;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import liquibase.logging.core.AbstractLogger;

@SuppressWarnings("squid:S2160")
public class JulLogger extends AbstractLogger {
  private static final String FQCN = JulLogger.class.getName();

  private final @Nullable String name;
  private final Logger logger;

  JulLogger(@Nullable String name) {
    this.name = name;
    this.logger = Logger.getLogger(name);
  }

  @Override
  public void severe(@Nullable String message) {
    log(Level.SEVERE, message, null);
  }

  @Override
  public void severe(@Nullable String message, @Nullable Throwable e) {
    log(Level.SEVERE, message, e);
  }

  @Override
  public void warning(@Nullable String message) {
    log(Level.WARNING, message, null);
  }

  @Override
  public void warning(@Nullable String message, @Nullable Throwable e) {
    log(Level.WARNING, message, e);
  }

  @Override
  public void info(@Nullable String message) {
    log(Level.INFO, message, null);
  }

  @Override
  public void info(@Nullable String message, @Nullable Throwable e) {
    log(Level.INFO, message, e);
  }

  @Override
  public void config(@Nullable String message) {
    log(Level.CONFIG, message, null);
  }

  @Override
  public void config(@Nullable String message, @Nullable Throwable e) {
    log(Level.CONFIG, message, e);
  }

  @Override
  public void fine(@Nullable String message) {
    log(Level.FINE, message, null);
  }

  @Override
  public void fine(@Nullable String message, @Nullable Throwable e) {
    log(Level.FINE, message, e);
  }

  @Override
  public void debug(@Nullable String message) {
    log(Level.FINE, message, null);
  }

  @Override
  public void debug(@Nullable String message, @Nullable Throwable e) {
    log(Level.FINE, message, e);
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
    Arrays.stream(new Throwable().getStackTrace())
        .filter(
            element -> {
              if (FQCN.equals(element.getClassName())) {
                found.set(true);
                return false;
              }
              return found.get();
            })
        .findFirst()
        .ifPresent(
            element -> {
              logRecord.setSourceClassName(element.getClassName());
              logRecord.setSourceMethodName(element.getMethodName());
            });
    logRecord.setResourceBundleName(logger.getResourceBundleName());
    logRecord.setResourceBundle(logger.getResourceBundle());
    return logRecord;
  }
}
