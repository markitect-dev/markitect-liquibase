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

import dev.markitect.liquibase.ScopeManagerHelper;
import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

public class SpringLiquibaseBeanPostProcessor implements InstantiationAwareBeanPostProcessor {
  private static final Log log = LogFactory.getLog(SpringLiquibaseBeanPostProcessor.class);

  private final Environment environment;

  public SpringLiquibaseBeanPostProcessor(Environment environment) {
    Assert.notNull(environment, "Environment must not be null");
    this.environment = environment;
  }

  @Override
  public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
    if (SpringLiquibase.class.isAssignableFrom(beanClass)
        && Boolean.TRUE.equals(
            environment.getProperty(
                "markitect.liquibase.use-thread-local-scope-manager", Boolean.class))) {
      log.debug("Initializing Liquibase scope manager");
      ScopeManagerHelper.useThreadLocalScopeManager();
    }
    return null;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) {
    if (bean instanceof SpringLiquibase) {
      log.debug("Providing Spring environment to Liquibase");
      SpringEnvironmentHolder.setEnvironment(environment);
    }
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) {
    if (bean instanceof SpringLiquibase) {
      SpringEnvironmentHolder.removeEnvironment();
    }
    return bean;
  }
}
