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

import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.inOrder;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class Log4jLoggerTests {
  @Mock private ExtendedLogger logger;

  @Test
  void test() {
    // given
    String fqcn = Log4jLogger.class.getName();
    var log4jLogger = new Log4jLogger(logger);

    // when
    var thrown = new Exception();
    log4jLogger.severe("s");
    log4jLogger.severe("s", thrown);
    log4jLogger.warning("w");
    log4jLogger.warning("w", thrown);
    log4jLogger.info("i");
    log4jLogger.info("i", thrown);
    log4jLogger.config("c");
    log4jLogger.config("c", thrown);
    log4jLogger.fine("f");
    log4jLogger.fine("f", thrown);
    log4jLogger.debug("d");
    log4jLogger.debug("d", thrown);
    log4jLogger.log(java.util.logging.Level.SEVERE, "s", null);
    log4jLogger.log(java.util.logging.Level.SEVERE, "s", thrown);
    log4jLogger.log(java.util.logging.Level.WARNING, "w", null);
    log4jLogger.log(java.util.logging.Level.INFO, "i", null);
    log4jLogger.log(java.util.logging.Level.CONFIG, "c", null);
    log4jLogger.log(java.util.logging.Level.FINE, "f", null);
    log4jLogger.log(java.util.logging.Level.FINER, "fr", null);
    log4jLogger.log(java.util.logging.Level.FINEST, "ft", null);
    log4jLogger.log(java.util.logging.Level.OFF, "o", null);
    log4jLogger.close();

    // then
    var inOrder = inOrder(logger);
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.ERROR, null, "s");
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.ERROR, null, "s", thrown);
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.WARN, null, "w");
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.WARN, null, "w", thrown);
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.INFO, null, "i");
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.INFO, null, "i", thrown);
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.DEBUG, null, "c");
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.DEBUG, null, "c", thrown);
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.DEBUG, null, "f");
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.DEBUG, null, "f", thrown);
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.DEBUG, null, "d");
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.DEBUG, null, "d", thrown);
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.ERROR, null, "s", (Throwable) null);
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.ERROR, null, "s", thrown);
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.WARN, null, "w", (Throwable) null);
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.INFO, null, "i", (Throwable) null);
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.DEBUG, null, "c", (Throwable) null);
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.DEBUG, null, "f", (Throwable) null);
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.DEBUG, null, "fr", (Throwable) null);
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.TRACE, null, "ft", (Throwable) null);
    then(logger).should(inOrder).logIfEnabled(fqcn, Level.OFF, null, "o", (Throwable) null);
    then(logger).shouldHaveNoMoreInteractions();
  }
}
