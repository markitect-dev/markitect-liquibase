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

import static dev.markitect.liquibase.util.Preconditions.checkNotNull;

import java.util.logging.Level;
import liquibase.logging.core.AbstractLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("squid:S2160")
public class Log4jLogger extends AbstractLogger {
  private static final String FQCN = Log4jLogger.class.getName();
  private static final int DEBUG_THRESHOLD = Level.FINER.intValue();
  private static final int INFO_THRESHOLD = Level.INFO.intValue();
  private static final int WARN_THRESHOLD = Level.WARNING.intValue();
  private static final int ERROR_THRESHOLD = Level.SEVERE.intValue();

  private final ExtendedLogger logger;

  public Log4jLogger(@Nullable String name) {
    this.logger = (ExtendedLogger) LogManager.getLogger(name);
  }

  @Override
  public void severe(@Nullable String message) {
    log(org.apache.logging.log4j.Level.ERROR, message, null);
  }

  @Override
  public void severe(@Nullable String message, @Nullable Throwable e) {
    log(org.apache.logging.log4j.Level.ERROR, message, e);
  }

  @Override
  public void warning(@Nullable String message) {
    log(org.apache.logging.log4j.Level.WARN, message, null);
  }

  @Override
  public void warning(@Nullable String message, @Nullable Throwable e) {
    log(org.apache.logging.log4j.Level.WARN, message, e);
  }

  @Override
  public void info(@Nullable String message) {
    log(org.apache.logging.log4j.Level.INFO, message, null);
  }

  @Override
  public void info(@Nullable String message, @Nullable Throwable e) {
    log(org.apache.logging.log4j.Level.INFO, message, e);
  }

  @Override
  public void config(@Nullable String message) {
    log(org.apache.logging.log4j.Level.DEBUG, message, null);
  }

  @Override
  public void config(@Nullable String message, @Nullable Throwable e) {
    log(org.apache.logging.log4j.Level.DEBUG, message, e);
  }

  @Override
  public void fine(@Nullable String message) {
    log(org.apache.logging.log4j.Level.DEBUG, message, null);
  }

  @Override
  public void fine(@Nullable String message, @Nullable Throwable e) {
    log(org.apache.logging.log4j.Level.DEBUG, message, e);
  }

  @Override
  public void debug(@Nullable String message) {
    log(org.apache.logging.log4j.Level.DEBUG, message, null);
  }

  @Override
  public void debug(@Nullable String message, @Nullable Throwable e) {
    log(org.apache.logging.log4j.Level.DEBUG, message, e);
  }

  @Override
  public void close() {
    // Redeclared to throw no exception
  }

  @Override
  public void log(Level level, @Nullable String message, @Nullable Throwable e) {
    checkNotNull(level);
    log(toLog4jLevel(level), message, e);
  }

  private void log(
      org.apache.logging.log4j.Level level, @Nullable String message, @Nullable Throwable e) {
    checkNotNull(level);
    logger.logIfEnabled(FQCN, level, null, message, e);
  }

  private org.apache.logging.log4j.Level toLog4jLevel(Level level) {
    checkNotNull(level);
    int value = level.intValue();
    if (value < DEBUG_THRESHOLD) {
      return org.apache.logging.log4j.Level.TRACE;
    }
    if (value < INFO_THRESHOLD) {
      return org.apache.logging.log4j.Level.DEBUG;
    }
    if (value < WARN_THRESHOLD) {
      return org.apache.logging.log4j.Level.INFO;
    }
    if (value < ERROR_THRESHOLD) {
      return org.apache.logging.log4j.Level.WARN;
    }
    return org.apache.logging.log4j.Level.ERROR;
  }
}
