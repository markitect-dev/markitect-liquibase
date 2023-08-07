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

package dev.markitect.liquibase.spring;

import java.sql.Connection;
import java.util.Optional;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.resource.ResourceAccessor;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.ReflectionUtils;

public class MarkitectSpringLiquibase extends SpringLiquibase implements DisposableBean {
  private boolean closeDataSourceOnceMigrated;
  protected boolean outputDefaultCatalog;
  protected boolean outputDefaultSchema;

  @Override
  protected Database createDatabase(@Nullable Connection c, ResourceAccessor resourceAccessor)
      throws DatabaseException {
    var database = super.createDatabase(c, resourceAccessor);
    database.setOutputDefaultCatalog(outputDefaultCatalog);
    database.setOutputDefaultSchema(outputDefaultSchema);
    return database;
  }

  @Override
  public void afterPropertiesSet() throws LiquibaseException {
    super.afterPropertiesSet();
    if (closeDataSourceOnceMigrated) {
      closeDataSource();
    }
  }

  private void closeDataSource() {
    Optional.ofNullable(ReflectionUtils.findMethod(getDataSource().getClass(), "close"))
        .ifPresent(closeMethod -> ReflectionUtils.invokeMethod(closeMethod, getDataSource()));
  }

  @Override
  public void destroy() {
    if (!this.closeDataSourceOnceMigrated) {
      closeDataSource();
    }
  }

  public void setCloseDataSourceOnceMigrated(boolean closeDataSourceOnceMigrated) {
    this.closeDataSourceOnceMigrated = closeDataSourceOnceMigrated;
  }

  public void setOutputDefaultCatalog(boolean outputDefaultCatalog) {
    this.outputDefaultCatalog = outputDefaultCatalog;
  }

  public void setOutputDefaultSchema(boolean outputDefaultSchema) {
    this.outputDefaultSchema = outputDefaultSchema;
  }
}
