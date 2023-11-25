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

import static org.mockito.Mockito.inOrder;

import java.util.logging.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
class Slf4jLoggerTests {
  @Mock private Logger logger;

  @Test
  void test() throws Exception {
    // given
    var loggerField = Slf4jLogger.class.getDeclaredField("logger");
    loggerField.setAccessible(true);
    var slf4jLogger = new Slf4jLogger<>(logger);
    loggerField.set(slf4jLogger, logger);

    // when
    var thrown = new Exception();
    slf4jLogger.severe("s");
    slf4jLogger.severe("s", thrown);
    slf4jLogger.warning("w");
    slf4jLogger.warning("w", thrown);
    slf4jLogger.info("i");
    slf4jLogger.info("i", thrown);
    slf4jLogger.config("c");
    slf4jLogger.config("c", thrown);
    slf4jLogger.fine("f");
    slf4jLogger.fine("f", thrown);
    slf4jLogger.debug("d");
    slf4jLogger.debug("d", thrown);
    slf4jLogger.log(Level.SEVERE, "s", null);
    slf4jLogger.log(Level.SEVERE, "s", thrown);
    slf4jLogger.log(Level.WARNING, "w", null);
    slf4jLogger.log(Level.INFO, "i", null);
    slf4jLogger.log(Level.CONFIG, "c", null);
    slf4jLogger.log(Level.FINE, "f", null);
    slf4jLogger.log(Level.FINER, "fr", null);
    slf4jLogger.log(Level.FINEST, "ft", null);
    slf4jLogger.close();

    // then
    var inOrder = inOrder(logger);
    inOrder.verify(logger).error("s");
    inOrder.verify(logger).error("s", thrown);
    inOrder.verify(logger).warn("w");
    inOrder.verify(logger).warn("w", thrown);
    inOrder.verify(logger).info("i");
    inOrder.verify(logger).info("i", thrown);
    inOrder.verify(logger).debug("c");
    inOrder.verify(logger).debug("c", thrown);
    inOrder.verify(logger).debug("f");
    inOrder.verify(logger).debug("f", thrown);
    inOrder.verify(logger).debug("d");
    inOrder.verify(logger).debug("d", thrown);
    inOrder.verify(logger).error("s", (Throwable) null);
    inOrder.verify(logger).error("s", thrown);
    inOrder.verify(logger).warn("w", (Throwable) null);
    inOrder.verify(logger).info("i", (Throwable) null);
    inOrder.verify(logger).debug("c", (Throwable) null);
    inOrder.verify(logger).debug("f", (Throwable) null);
    inOrder.verify(logger).debug("fr", (Throwable) null);
    inOrder.verify(logger).trace("ft", (Throwable) null);
    inOrder.verifyNoMoreInteractions();
  }
}
