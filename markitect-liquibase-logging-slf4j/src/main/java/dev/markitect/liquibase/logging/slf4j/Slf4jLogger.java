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

package dev.markitect.liquibase.logging.slf4j;

import static dev.markitect.liquibase.util.Preconditions.checkNotNull;

import java.util.logging.Level;
import liquibase.logging.core.AbstractLogger;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jLogger extends AbstractLogger {
  private static final int DEBUG_THRESHOLD = Level.FINE.intValue();
  private static final int INFO_THRESHOLD = Level.INFO.intValue();
  private static final int WARN_THRESHOLD = Level.WARNING.intValue();
  private static final int ERROR_THRESHOLD = Level.SEVERE.intValue();

  private final Logger logger;

  Slf4jLogger(Class<?> clazz) {
    checkNotNull(clazz);
    this.logger = LoggerFactory.getLogger(clazz);
  }

  @Override
  public void severe(@Nullable String message) {
    logger.error(message);
  }

  @Override
  public void severe(@Nullable String message, @Nullable Throwable e) {
    logger.error(message, e);
  }

  @Override
  public void warning(@Nullable String message) {
    logger.warn(message);
  }

  @Override
  public void warning(@Nullable String message, @Nullable Throwable e) {
    logger.warn(message, e);
  }

  @Override
  public void info(@Nullable String message) {
    logger.info(message);
  }

  @Override
  public void info(@Nullable String message, @Nullable Throwable e) {
    logger.info(message, e);
  }

  @Override
  public void config(@Nullable String message) {
    logger.debug(message);
  }

  @Override
  public void config(@Nullable String message, @Nullable Throwable e) {
    logger.debug(message, e);
  }

  @Override
  public void fine(@Nullable String message) {
    logger.debug(message);
  }

  @Override
  public void fine(@Nullable String message, @Nullable Throwable e) {
    logger.debug(message, e);
  }

  @Override
  public void debug(@Nullable String message) {
    logger.debug(message);
  }

  @Override
  public void debug(@Nullable String message, @Nullable Throwable e) {
    logger.debug(message, e);
  }

  @Override
  public void close() {}

  @Override
  public void log(Level level, @Nullable String message, @Nullable Throwable e) {
    checkNotNull(level);
    int value = level.intValue();
    if (value < DEBUG_THRESHOLD) {
      logger.trace(message, e);
    } else if (value < INFO_THRESHOLD) {
      logger.debug(message, e);
    } else if (value < WARN_THRESHOLD) {
      logger.info(message, e);
    } else if (value < ERROR_THRESHOLD) {
      logger.warn(message, e);
    } else {
      logger.error(message, e);
    }
  }
}
