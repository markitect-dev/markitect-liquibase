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

import java.util.logging.Level;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.spi.LocationAwareLogger;

@ExtendWith(MockitoExtension.class)
class Slf4jLocationAwareLoggerTests {
  @Mock private LocationAwareLogger logger;

  @Test
  void test() throws Exception {
    // given
    String fqcn = Slf4jLocationAwareLogger.class.getName();
    var loggerField = Slf4jLogger.class.getDeclaredField("logger");
    loggerField.setAccessible(true);
    var slf4jLogger = new Slf4jLocationAwareLogger(logger);
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
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.ERROR_INT, "s", null, null);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.ERROR_INT, "s", null, thrown);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.WARN_INT, "w", null, null);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.WARN_INT, "w", null, thrown);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.INFO_INT, "i", null, null);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.INFO_INT, "i", null, thrown);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.DEBUG_INT, "c", null, null);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.DEBUG_INT, "c", null, thrown);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.DEBUG_INT, "f", null, null);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.DEBUG_INT, "f", null, thrown);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.DEBUG_INT, "d", null, null);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.DEBUG_INT, "d", null, thrown);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.ERROR_INT, "s", null, null);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.ERROR_INT, "s", null, thrown);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.WARN_INT, "w", null, null);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.INFO_INT, "i", null, null);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.DEBUG_INT, "c", null, null);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.DEBUG_INT, "f", null, null);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.DEBUG_INT, "fr", null, null);
    then(logger).should(inOrder).log(null, fqcn, LocationAwareLogger.TRACE_INT, "ft", null, null);
    then(logger).shouldHaveNoMoreInteractions();
  }
}
