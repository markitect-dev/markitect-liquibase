/*
 * Copyright 2023-2024 Markitect
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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.logging.Level;
import org.jspecify.annotations.Nullable;
import org.slf4j.spi.LocationAwareLogger;

public class Slf4jLocationAwareLogger extends Slf4jLogger<LocationAwareLogger> {
  private static final String FQCN = Slf4jLocationAwareLogger.class.getName();

  private static int toSlf4jLevel(Level level) {
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

  public Slf4jLocationAwareLogger(LocationAwareLogger logger) {
    super(logger);
  }

  @Override
  public void severe(@Nullable String message) {
    logger.log(null, FQCN, LocationAwareLogger.ERROR_INT, message, null, null);
  }

  @Override
  public void severe(@Nullable String message, @Nullable Throwable e) {
    logger.log(null, FQCN, LocationAwareLogger.ERROR_INT, message, null, e);
  }

  @Override
  public void warning(@Nullable String message) {
    logger.log(null, FQCN, LocationAwareLogger.WARN_INT, message, null, null);
  }

  @Override
  public void warning(@Nullable String message, @Nullable Throwable e) {
    logger.log(null, FQCN, LocationAwareLogger.WARN_INT, message, null, e);
  }

  @Override
  public void info(@Nullable String message) {
    logger.log(null, FQCN, LocationAwareLogger.INFO_INT, message, null, null);
  }

  @Override
  public void info(@Nullable String message, @Nullable Throwable e) {
    logger.log(null, FQCN, LocationAwareLogger.INFO_INT, message, null, e);
  }

  @Override
  public void config(@Nullable String message) {
    logger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, message, null, null);
  }

  @Override
  public void config(@Nullable String message, @Nullable Throwable e) {
    logger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, message, null, e);
  }

  @Override
  public void fine(@Nullable String message) {
    logger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, message, null, null);
  }

  @Override
  public void fine(@Nullable String message, @Nullable Throwable e) {
    logger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, message, null, e);
  }

  @Override
  public void debug(@Nullable String message) {
    logger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, message, null, null);
  }

  @Override
  public void debug(@Nullable String message, @Nullable Throwable e) {
    logger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, message, null, e);
  }

  @Override
  public void log(Level level, @Nullable String message, @Nullable Throwable e) {
    checkNotNull(level);
    if (level.equals(Level.OFF)) {
      return;
    }
    logger.log(null, FQCN, toSlf4jLevel(level), message, null, e);
  }
}
