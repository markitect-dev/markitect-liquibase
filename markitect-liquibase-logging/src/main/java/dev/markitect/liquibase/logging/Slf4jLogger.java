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
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

public class Slf4jLogger extends AbstractLogger {
  private static final String FQCN = Slf4jLogger.class.getName();
  private static final int DEBUG_THRESHOLD = Level.FINER.intValue();
  private static final int INFO_THRESHOLD = Level.INFO.intValue();
  private static final int WARN_THRESHOLD = Level.WARNING.intValue();
  private static final int ERROR_THRESHOLD = Level.SEVERE.intValue();

  private final LocationAwareLogger logger;

  public Slf4jLogger(@Nullable String name) {
    this.logger = (LocationAwareLogger) LoggerFactory.getLogger(name);
  }

  @Override
  public void severe(@Nullable String message) {
    log(LocationAwareLogger.ERROR_INT, message, null);
  }

  @Override
  public void severe(@Nullable String message, @Nullable Throwable e) {
    log(LocationAwareLogger.ERROR_INT, message, e);
  }

  @Override
  public void warning(@Nullable String message) {
    log(LocationAwareLogger.WARN_INT, message, null);
  }

  @Override
  public void warning(@Nullable String message, @Nullable Throwable e) {
    log(LocationAwareLogger.WARN_INT, message, e);
  }

  @Override
  public void info(@Nullable String message) {
    log(LocationAwareLogger.INFO_INT, message, null);
  }

  @Override
  public void info(@Nullable String message, @Nullable Throwable e) {
    log(LocationAwareLogger.INFO_INT, message, e);
  }

  @Override
  public void config(@Nullable String message) {
    log(LocationAwareLogger.DEBUG_INT, message, null);
  }

  @Override
  public void config(@Nullable String message, @Nullable Throwable e) {
    log(LocationAwareLogger.DEBUG_INT, message, e);
  }

  @Override
  public void fine(@Nullable String message) {
    log(LocationAwareLogger.DEBUG_INT, message, null);
  }

  @Override
  public void fine(@Nullable String message, @Nullable Throwable e) {
    log(LocationAwareLogger.DEBUG_INT, message, e);
  }

  @Override
  public void debug(@Nullable String message) {
    log(LocationAwareLogger.DEBUG_INT, message, null);
  }

  @Override
  public void debug(@Nullable String message, @Nullable Throwable e) {
    log(LocationAwareLogger.DEBUG_INT, message, e);
  }

  @Override
  public void close() {}

  @Override
  public void log(Level level, @Nullable String message, @Nullable Throwable e) {
    checkNotNull(level);
    log(toSlf4jLevel(level), message, e);
  }

  private void log(int level, @Nullable String message, @Nullable Throwable e) {
    logger.log(null, FQCN, level, message, null, e);
  }

  private int toSlf4jLevel(Level level) {
    checkNotNull(level);
    int value = level.intValue();
    if (value < DEBUG_THRESHOLD) {
      return LocationAwareLogger.TRACE_INT;
    }
    if (value < INFO_THRESHOLD) {
      return LocationAwareLogger.DEBUG_INT;
    }
    if (value < WARN_THRESHOLD) {
      return LocationAwareLogger.INFO_INT;
    }
    if (value < ERROR_THRESHOLD) {
      return LocationAwareLogger.WARN_INT;
    }
    return LocationAwareLogger.ERROR_INT;
  }
}
