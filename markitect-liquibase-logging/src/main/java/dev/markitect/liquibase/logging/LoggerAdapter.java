/*
 * Copyright 2023-2025 Markitect
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

import liquibase.logging.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.LoggerContext;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

public class LoggerAdapter {
  private static final LoggingApi LOGGING_API;

  static {
    if (isPresent("org.apache.logging.log4j.spi.ExtendedLogger")
        && !isPresent("org.apache.logging.slf4j.SLF4JProvider")) {
      LOGGING_API = LoggingApi.LOG4J;
    } else if (isPresent("org.slf4j.spi.LocationAwareLogger")
        && !isPresent("org.slf4j.jul.JULServiceProvider")) {
      LOGGING_API = LoggingApi.SLF4J;
    } else {
      LOGGING_API = LoggingApi.JUL;
    }
  }

  private static boolean isPresent(String className) {
    try {
      Class.forName(className);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  public static Logger getLogger(String name) {
    checkNotNull(name);
    return switch (LOGGING_API) {
      case LOG4J -> Log4jAdapter.getLogger(name);
      case SLF4J -> Slf4jAdapter.getLogger(name);
      default -> JulAdapter.getLogger(name);
    };
  }

  private enum LoggingApi {
    JUL,
    LOG4J,
    SLF4J
  }

  private static class JulAdapter {
    static Logger getLogger(String name) {
      checkNotNull(name);
      return new JulLogger(name);
    }

    private JulAdapter() {}
  }

  private static class Log4jAdapter {
    private static final LoggerContext context =
        LogManager.getContext(Log4jAdapter.class.getClassLoader(), /* currentContext= */ false);

    static Logger getLogger(String name) {
      checkNotNull(name);
      return new Log4jLogger(context.getLogger(name));
    }

    private Log4jAdapter() {}
  }

  private static class Slf4jAdapter {
    static Logger getLogger(String name) {
      checkNotNull(name);
      var logger = LoggerFactory.getLogger(name);
      if (logger instanceof LocationAwareLogger locationAwareLogger) {
        return new Slf4jLocationAwareLogger(locationAwareLogger);
      }
      return new Slf4jLogger<>(logger);
    }

    private Slf4jAdapter() {}
  }

  private LoggerAdapter() {}
}
