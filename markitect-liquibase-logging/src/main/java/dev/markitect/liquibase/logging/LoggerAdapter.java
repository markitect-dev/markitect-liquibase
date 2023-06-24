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

import liquibase.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LoggerAdapter {
  private static final LoggingApi LOGGING_API;

  static {
    if (isPresent("org.apache.logging.log4j.spi.ExtendedLogger")
        && !isPresent("org.apache.logging.slf4j.SLF4JProvider")) {
      LOGGING_API = LoggingApi.LOG4J;
    } else if (isPresent("org.slf4j.spi.LocationAwareLogger")) {
      LOGGING_API = LoggingApi.SLF4J;
    } else {
      LOGGING_API = LoggingApi.JUL;
    }
  }

  private static boolean isPresent(String className) {
    checkNotNull(className);
    try {
      Class.forName(className);
    } catch (ClassNotFoundException e) {
      return false;
    }
    return true;
  }

  public static Logger getLogger(@Nullable String name) {
    switch (LOGGING_API) {
      case SLF4J:
        return Slf4jAdapter.getLogger(name);
      case LOG4J:
        return Log4jAdapter.getLogger(name);
      default:
        return JulAdapter.getLogger(name);
    }
  }

  private enum LoggingApi {
    JUL,
    LOG4J,
    SLF4J
  }

  private static class JulAdapter {
    public static Logger getLogger(String name) {
      return new JulLogger(name);
    }
  }

  private static class Log4jAdapter {
    public static Logger getLogger(String name) {
      return new Log4jLogger(name);
    }
  }

  private static class Slf4jAdapter {
    public static Logger getLogger(String name) {
      return new Slf4jLogger(name);
    }
  }

  private LoggerAdapter() {}
}
