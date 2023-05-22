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
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import liquibase.logging.core.AbstractLogger;
import org.checkerframework.checker.nullness.qual.Nullable;

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
  public void close() {}

  @Override
  public void log(Level level, @Nullable String message, @Nullable Throwable e) {
    checkNotNull(level);
    if (logger.isLoggable(level)) {
      logger.log(toLogRecord(level, message, e));
    }
  }

  @SuppressWarnings("ReassignedVariable")
  private LogRecord toLogRecord(Level level, @Nullable String message, @Nullable Throwable e) {
    checkNotNull(level);
    @Nullable String sourceClassName = null;
    @Nullable String sourceMethodName = null;
    boolean found = false;
    for (StackTraceElement element : new Throwable().getStackTrace()) {
      String className = element.getClassName();
      if (FQCN.equals(className)) {
        found = true;
      } else if (found) {
        sourceClassName = className;
        sourceMethodName = element.getMethodName();
        break;
      }
    }
    LogRecord logRecord = new LogRecord(level, message);
    logRecord.setLoggerName(name);
    logRecord.setThrown(e);
    logRecord.setSourceClassName(sourceClassName);
    logRecord.setSourceMethodName(sourceMethodName);
    logRecord.setResourceBundleName(logger.getResourceBundleName());
    logRecord.setResourceBundle(logger.getResourceBundle());
    return logRecord;
  }
}
