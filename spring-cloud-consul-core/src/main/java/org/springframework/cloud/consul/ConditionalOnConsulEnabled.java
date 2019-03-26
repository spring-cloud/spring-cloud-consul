/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.consul;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.ecwid.consul.v1.ConsulClient;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

/**
 * When both property and consul classes are on the classpath.
 * @author Spencer Gibb
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Conditional(ConditionalOnConsulEnabled.OnConsulEnabledCondition.class)
public @interface ConditionalOnConsulEnabled {

	/**
	 * Verifies multiple conditions to see if Consul should be enabled.
	 */
	class OnConsulEnabledCondition extends AllNestedConditions {

		OnConsulEnabledCondition() {
			super(ConfigurationPhase.REGISTER_BEAN);
		}

		/**
		 * Consul property is enabled.
		 */
		@ConditionalOnProperty(value = "spring.cloud.consul.enabled", matchIfMissing = true)
		static class FoundProperty {

		}

		/**
		 * Consul client class found.
		 */
		@ConditionalOnClass(ConsulClient.class)
		static class FoundClass {

		}

	}

}
