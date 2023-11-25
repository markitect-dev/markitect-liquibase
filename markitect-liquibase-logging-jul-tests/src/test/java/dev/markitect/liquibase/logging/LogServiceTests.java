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

import java.util.logging.LogManager;
import liquibase.Scope;
import liquibase.changelog.visitor.UpdateVisitor;
import liquibase.changelog.visitor.ValidatingVisitor;
import liquibase.logging.LogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class LogServiceTests {
  @BeforeEach
  void setUp() throws Exception {
    try (var ins =
        LogServiceTests.class.getClassLoader().getResourceAsStream("logging.properties")) {
      LogManager.getLogManager().readConfiguration(ins);
    }
  }

  @AfterEach
  void tearDown() throws Exception {
    LogManager.getLogManager().readConfiguration();
  }

  @Test
  void logs(CapturedOutput output) {
    // when
    var logService = Scope.getCurrentScope().get(Scope.Attr.logService, LogService.class);

    // then
    assertThat(logService).isInstanceOf(MarkitectLogService.class);

    // when
    var log = Scope.getCurrentScope().getLog(UpdateVisitor.class);
    var log2 = Scope.getCurrentScope().getLog(ValidatingVisitor.class);

    // then
    assertThat(log).isInstanceOf(JulLogger.class);
    assertThat(log2).isInstanceOf(JulLogger.class);

    // when
    log.fine("Running Changeset: filePath::id::author");
    var e = new Exception("Preconditions Failed");
    log2.fine("Precondition failed: " + e.getMessage(), e);

    // then
    assertThat(output)
        .contains(
            " FINE liquibase.changelog.visitor.UpdateVisitor - "
                + "Running Changeset: filePath::id::author",
            " FINE liquibase.changelog.visitor.ValidatingVisitor - "
                + "Precondition failed: Preconditions Failed",
            "java.lang.Exception: Preconditions Failed");
  }
}
