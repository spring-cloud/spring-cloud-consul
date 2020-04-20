/*
 * Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.consul.serviceregistry;

import java.util.List;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * @author Spencer Gibb
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(AutoServiceRegistrationProperties.class)
@ConditionalOnMissingBean(
		type = "org.springframework.cloud.consul.discovery.ConsulLifecycle")
@ConditionalOnConsulEnabled
@Conditional(ConsulAutoServiceRegistrationAutoConfiguration.OnConsulRegistrationEnabledCondition.class)
@AutoConfigureAfter({ AutoServiceRegistrationConfiguration.class,
		ConsulServiceRegistryAutoConfiguration.class })
public class ConsulAutoServiceRegistrationAutoConfiguration {

	@Autowired
	AutoServiceRegistrationProperties autoServiceRegistrationProperties;

	@Bean
	@ConditionalOnMissingBean
	public ConsulAutoServiceRegistration consulAutoServiceRegistration(
			ConsulServiceRegistry registry,
			AutoServiceRegistrationProperties autoServiceRegistrationProperties,
			ConsulDiscoveryProperties properties,
			ConsulAutoRegistration consulRegistration) {
		return new ConsulAutoServiceRegistration(registry,
				autoServiceRegistrationProperties, properties, consulRegistration);
	}

	@Bean
	public ConsulAutoServiceRegistrationListener consulAutoServiceRegistrationListener(
			ConsulAutoServiceRegistration registration) {
		return new ConsulAutoServiceRegistrationListener(registration);
	}

	@Bean
	@ConditionalOnMissingBean
	public ConsulAutoRegistration consulRegistration(
			AutoServiceRegistrationProperties autoServiceRegistrationProperties,
			ConsulDiscoveryProperties properties, ApplicationContext applicationContext,
			ObjectProvider<List<ConsulRegistrationCustomizer>> registrationCustomizers,
			ObjectProvider<List<ConsulManagementRegistrationCustomizer>> managementRegistrationCustomizers,
			HeartbeatProperties heartbeatProperties) {
		return ConsulAutoRegistration.registration(autoServiceRegistrationProperties,
				properties, applicationContext, registrationCustomizers.getIfAvailable(),
				managementRegistrationCustomizers.getIfAvailable(), heartbeatProperties);
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(ServletContext.class)
	protected static class ConsulServletConfiguration {

		@Bean
		public ConsulRegistrationCustomizer servletConsulCustomizer(
				ObjectProvider<ServletContext> servletContext) {
			return new ConsulServletRegistrationCustomizer(servletContext);
		}

	}

	protected static class OnConsulRegistrationEnabledCondition
			extends AllNestedConditions {

		OnConsulRegistrationEnabledCondition() {
			super(ConfigurationPhase.REGISTER_BEAN);
		}

		@ConditionalOnProperty(
				value = "spring.cloud.service-registry.auto-registration.enabled",
				matchIfMissing = true)
		static class AutoRegistrationEnabledClass {

		}

		@ConditionalOnProperty(
				value = "spring.cloud.consul.service-registry.auto-registration.enabled",
				matchIfMissing = true)
		static class ConsulAutoRegistrationEnabledClass {

		}

		@ConditionalOnProperty(value = "spring.cloud.service-registry.enabled",
				matchIfMissing = true)
		static class ServiceRegistryEnabledClass {

		}

		@ConditionalOnProperty(value = "spring.cloud.consul.service-registry.enabled",
				matchIfMissing = true)
		static class ConsulServiceRegistryEnabledClass {

		}

	}

}
