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

import static dev.markitect.liquibase.base.Preconditions.checkIndex;
import static dev.markitect.liquibase.base.Preconditions.checkNotNull;
import static dev.markitect.liquibase.spring.SpringEnvironmentHolder.getEnvironment;

import dev.markitect.liquibase.base.Nullable;
import dev.markitect.liquibase.base.Preconditions;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import liquibase.configuration.AbstractConfigurationValueProvider;
import liquibase.configuration.ProvidedValue;

public class SpringEnvironmentValueProvider extends AbstractConfigurationValueProvider {
  private static final String PROPERTY_PREFIX = "markitect.liquibase.properties.";
  private static final String SOURCE_DESCRIPTION =
      SpringEnvironmentValueProvider.class.getSimpleName();

  @Override
  public int getPrecedence() {
    return 250;
  }

  @Override
  public @Nullable ProvidedValue getProvidedValue(String... keyAndAliases) {
    checkNotNull(keyAndAliases);
    String actualKey = checkNotNull(keyAndAliases[checkIndex(0, keyAndAliases.length)]);
    return Arrays.stream(keyAndAliases)
        .map(Preconditions::checkNotNull)
        .map(
            requestedKey ->
                Optional.ofNullable(getEnvironment())
                    .map(environment -> environment.getProperty(PROPERTY_PREFIX + requestedKey))
                    .map(
                        value ->
                            new ProvidedValue(
                                requestedKey, actualKey, value, SOURCE_DESCRIPTION, this))
                    .orElse(null))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }
}
