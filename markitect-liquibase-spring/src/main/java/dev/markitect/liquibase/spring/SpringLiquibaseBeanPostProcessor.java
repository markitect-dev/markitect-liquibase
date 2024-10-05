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

package dev.markitect.liquibase.spring;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

public class SpringLiquibaseBeanPostProcessor implements BeanPostProcessor {
  private static final Log log = LogFactory.getLog(SpringLiquibaseBeanPostProcessor.class);

  private final Environment environment;

  public SpringLiquibaseBeanPostProcessor(Environment environment) {
    Assert.notNull(environment, "Environment must not be null");
    this.environment = environment;
  }

  @CanIgnoreReturnValue
  @Override
  @SuppressWarnings("NullableProblems")
  public @Nullable Object postProcessBeforeInitialization(Object bean, String beanName) {
    if (bean instanceof SpringLiquibase) {
      log.debug("Providing Spring environment to Liquibase");
      SpringEnvironmentHolder.setEnvironment(environment);
    }
    return bean;
  }

  @CanIgnoreReturnValue
  @Override
  @SuppressWarnings("NullableProblems")
  public @Nullable Object postProcessAfterInitialization(Object bean, String beanName) {
    if (bean instanceof SpringLiquibase) {
      SpringEnvironmentHolder.removeEnvironment();
    }
    return bean;
  }
}
