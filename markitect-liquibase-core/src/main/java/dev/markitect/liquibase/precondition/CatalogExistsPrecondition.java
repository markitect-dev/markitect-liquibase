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

package dev.markitect.liquibase.precondition;

import static dev.markitect.liquibase.base.Preconditions.checkNotNull;
import static dev.markitect.liquibase.database.Databases.catalogExists;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.precondition.AbstractPrecondition;
import org.checkerframework.checker.nullness.qual.Nullable;

public class CatalogExistsPrecondition extends AbstractPrecondition {
  private @Nullable String catalogName;

  public @Nullable String getCatalogName() {
    return catalogName;
  }

  public void setCatalogName(@Nullable String catalogName) {
    this.catalogName = catalogName;
  }

  @Override
  public Warnings warn(Database database) {
    checkNotNull(database);
    return new Warnings();
  }

  @Override
  public ValidationErrors validate(Database database) {
    checkNotNull(database);
    var errors = new ValidationErrors();
    errors.checkRequiredField("catalogName", catalogName);
    return errors;
  }

  @Override
  public void check(
      Database database,
      @Nullable DatabaseChangeLog changeLog,
      @Nullable ChangeSet changeSet,
      @Nullable ChangeExecListener changeExecListener)
      throws PreconditionFailedException, PreconditionErrorException {
    checkNotNull(database);
    try {
      if (!catalogExists(database, catalogName)) {
        throw new PreconditionFailedException(
            "Catalog %s does not exist".formatted(catalogName), changeLog, this);
      }
    } catch (DatabaseException | RuntimeException e) {
      throw new PreconditionErrorException(e, changeLog, this);
    }
  }

  @Override
  public String getSerializedObjectNamespace() {
    return GENERIC_CHANGELOG_EXTENSION_NAMESPACE;
  }

  @Override
  public String getName() {
    return "catalogExists";
  }
}
