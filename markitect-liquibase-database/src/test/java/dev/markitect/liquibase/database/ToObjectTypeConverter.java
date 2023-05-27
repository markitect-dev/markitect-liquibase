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

package dev.markitect.liquibase.database;

import liquibase.structure.DatabaseObject;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;

class ToObjectTypeConverter implements ArgumentConverter {
  @Override
  @SuppressWarnings("unchecked")
  public Class<? extends DatabaseObject> convert(Object source, ParameterContext context)
      throws ArgumentConversionException {
    if (source == null) {
      return null;
    }
    if (source instanceof String) {
      try {
        Class<?> clazz = Class.forName("liquibase.structure.core." + source);
        if (DatabaseObject.class.isAssignableFrom(clazz)) {
          return (Class<? extends DatabaseObject>) clazz;
        }
      } catch (ClassNotFoundException e) {
        // ignore
      }
    }
    throw new ArgumentConversionException("Cannot convert to object type: " + source);
  }
}
