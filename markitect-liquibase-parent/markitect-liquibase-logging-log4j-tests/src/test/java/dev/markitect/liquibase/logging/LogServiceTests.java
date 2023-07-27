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

import liquibase.Scope;
import liquibase.changelog.visitor.UpdateVisitor;
import liquibase.changelog.visitor.ValidatingVisitor;
import liquibase.logging.LogService;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class LogServiceTests {
  @Test
  @StdIo
  void logs(StdOut out) {
    // when
    var logService = Scope.getCurrentScope().get(Scope.Attr.logService, LogService.class);

    // then
    assertThat(logService).isInstanceOf(MarkitectLogService.class);

    // when
    var log = Scope.getCurrentScope().getLog(UpdateVisitor.class);
    var log2 = Scope.getCurrentScope().getLog(ValidatingVisitor.class);

    // then
    assertThat(log).isInstanceOf(Log4jLogger.class);
    assertThat(log2).isInstanceOf(Log4jLogger.class);

    // when
    log.fine("Running Changeset: filePath::id::author");
    var e = new Exception("Preconditions Failed");
    log2.fine("Precondition failed: " + e.getMessage(), e);

    // then
    assertThat(String.join("\n", out.capturedLines()))
        .contains(
            " [main] DEBUG liquibase.changelog.visitor.UpdateVisitor - "
                + "Running Changeset: filePath::id::author",
            " [main] DEBUG liquibase.changelog.visitor.ValidatingVisitor - "
                + "Precondition failed: Preconditions Failed",
            "java.lang.Exception: Preconditions Failed");
  }
}
