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
import java.util.logging.Logger;
import liquibase.logging.core.AbstractLogger;
import org.checkerframework.checker.nullness.qual.Nullable;

public class JulLogger extends AbstractLogger {
  private final Logger logger;

  JulLogger(@Nullable String name) {
    this.logger = Logger.getLogger(name);
  }

  @Override
  public void severe(@Nullable String message) {
    logger.log(Level.SEVERE, message);
  }

  @Override
  public void severe(@Nullable String message, @Nullable Throwable e) {
    logger.log(Level.SEVERE, message, e);
  }

  @Override
  public void warning(@Nullable String message) {
    logger.log(Level.WARNING, message);
  }

  @Override
  public void warning(@Nullable String message, @Nullable Throwable e) {
    logger.log(Level.WARNING, message, e);
  }

  @Override
  public void info(@Nullable String message) {
    logger.log(Level.INFO, message);
  }

  @Override
  public void info(@Nullable String message, @Nullable Throwable e) {
    logger.log(Level.INFO, message, e);
  }

  @Override
  public void config(@Nullable String message) {
    logger.log(Level.CONFIG, message);
  }

  @Override
  public void config(@Nullable String message, @Nullable Throwable e) {
    logger.log(Level.CONFIG, message, e);
  }

  @Override
  public void fine(@Nullable String message) {
    logger.log(Level.FINE, message);
  }

  @Override
  public void fine(@Nullable String message, @Nullable Throwable e) {
    logger.log(Level.FINE, message, e);
  }

  @Override
  public void debug(@Nullable String message) {
    logger.log(Level.FINE, message);
  }

  @Override
  public void debug(@Nullable String message, @Nullable Throwable e) {
    logger.log(Level.FINE, message, e);
  }

  @Override
  public void close() {}

  @Override
  public void log(Level level, @Nullable String message, @Nullable Throwable e) {
    checkNotNull(level);
    logger.log(level, message, e);
  }
}
