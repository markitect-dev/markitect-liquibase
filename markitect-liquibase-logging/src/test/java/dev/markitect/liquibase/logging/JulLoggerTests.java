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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JulLoggerTests {
  @Mock private Logger logger;
  @Mock private ResourceBundle rb;
  @Captor private ArgumentCaptor<Level> levelCaptor;
  @Captor private ArgumentCaptor<LogRecord> logRecordCaptor;

  @Test
  void test() throws Exception {
    // given
    String name = JulLoggerTests.class.getName();
    String sourceClassName = JulLoggerTests.class.getName();
    given(logger.isLoggable(any()))
        .willAnswer(
            invocation -> invocation.<Level>getArgument(0).intValue() >= Level.FINER.intValue());
    given(logger.getResourceBundleName()).willReturn("rb");
    given(logger.getResourceBundle()).willReturn(rb);
    var loggerField = JulLogger.class.getDeclaredField("logger");
    loggerField.setAccessible(true);
    var julLogger = new JulLogger(name);
    loggerField.set(julLogger, logger);

    // when
    var thrown = new Exception();
    julLogger.severe("s");
    julLogger.severe("s", thrown);
    julLogger.warning("w");
    julLogger.warning("w", thrown);
    julLogger.info("i");
    julLogger.info("i", thrown);
    julLogger.config("c");
    julLogger.config("c", thrown);
    julLogger.fine("f");
    julLogger.fine("f", thrown);
    julLogger.debug("d");
    julLogger.debug("d", thrown);
    julLogger.log(Level.FINER, "fr", null);
    julLogger.log(Level.FINEST, "ft", null);
    julLogger.close();

    // then
    verify(logger, times(14)).isLoggable(levelCaptor.capture());
    verify(logger, times(13)).log(logRecordCaptor.capture());
    verifyNoMoreInteractions(logger);
    assertThat(levelCaptor.getAllValues())
        .containsExactly(
            Level.SEVERE,
            Level.SEVERE,
            Level.WARNING,
            Level.WARNING,
            Level.INFO,
            Level.INFO,
            Level.CONFIG,
            Level.CONFIG,
            Level.FINE,
            Level.FINE,
            Level.FINE,
            Level.FINE,
            Level.FINER,
            Level.FINEST);
    assertThat(logRecordCaptor.getAllValues())
        .extracting(
            LogRecord::getLevel,
            LogRecord::getMessage,
            LogRecord::getLoggerName,
            LogRecord::getThrown,
            LogRecord::getSourceClassName,
            LogRecord::getSourceMethodName,
            LogRecord::getResourceBundleName,
            LogRecord::getResourceBundle)
        .containsExactly(
            tuple(Level.SEVERE, "s", name, null, sourceClassName, "test", "rb", rb),
            tuple(Level.SEVERE, "s", name, thrown, sourceClassName, "test", "rb", rb),
            tuple(Level.WARNING, "w", name, null, sourceClassName, "test", "rb", rb),
            tuple(Level.WARNING, "w", name, thrown, sourceClassName, "test", "rb", rb),
            tuple(Level.INFO, "i", name, null, sourceClassName, "test", "rb", rb),
            tuple(Level.INFO, "i", name, thrown, sourceClassName, "test", "rb", rb),
            tuple(Level.CONFIG, "c", name, null, sourceClassName, "test", "rb", rb),
            tuple(Level.CONFIG, "c", name, thrown, sourceClassName, "test", "rb", rb),
            tuple(Level.FINE, "f", name, null, sourceClassName, "test", "rb", rb),
            tuple(Level.FINE, "f", name, thrown, sourceClassName, "test", "rb", rb),
            tuple(Level.FINE, "d", name, null, sourceClassName, "test", "rb", rb),
            tuple(Level.FINE, "d", name, thrown, sourceClassName, "test", "rb", rb),
            tuple(Level.FINER, "fr", name, null, sourceClassName, "test", "rb", rb));
  }
}
