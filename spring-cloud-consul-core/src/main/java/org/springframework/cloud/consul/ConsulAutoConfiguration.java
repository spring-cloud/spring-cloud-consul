/*
 * Copyright 2013-2025 the original author or authors.
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

import java.util.function.Supplier;

import com.ecwid.consul.transport.TLSConfig;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.ConsulRawClient;
import com.ecwid.consul.v1.ConsulRawClient.Builder;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.util.StringUtils;

/**
 * @author Spencer Gibb
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnConsulEnabled
public class ConsulAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ConsulProperties consulProperties() {
		return new ConsulProperties();
	}

	@Bean
	@ConditionalOnMissingBean(value = ConsulRawClient.Builder.class, parameterizedContainer = Supplier.class)
	public Supplier<ConsulRawClient.Builder> consulRawClientBuilderSupplier() {
		return createConsulRawClientBuilder();
	}

	@Bean
	@ConditionalOnMissingBean
	public ConsulClient consulClient(ConsulProperties consulProperties,
			Supplier<ConsulRawClient.Builder> consulRawClientBuilderSupplier) {
		return createConsulClient(consulProperties, consulRawClientBuilderSupplier);
	}

	public static Supplier<Builder> createConsulRawClientBuilder() {
		return Builder::builder;
	}

	public static ConsulClient createConsulClient(ConsulProperties consulProperties,
			Supplier<ConsulRawClient.Builder> consulRawClientBuilderSupplier) {
		ConsulRawClient.Builder builder = consulRawClientBuilderSupplier.get();
		final String agentPath = consulProperties.getPath();
		final String agentHost = StringUtils.hasLength(consulProperties.getScheme())
				? consulProperties.getScheme() + "://" + consulProperties.getHost() : consulProperties.getHost();
		builder.setHost(agentHost).setPort(consulProperties.getPort());

		if (consulProperties.getTls() != null) {
			ConsulProperties.TLSConfig tls = consulProperties.getTls();
			TLSConfig tlsConfig = new TLSConfig(tls.getKeyStoreInstanceType(), tls.getCertificatePath(),
					tls.getCertificatePassword(), tls.getKeyStorePath(), tls.getKeyStorePassword());
			builder.setTlsConfig(tlsConfig);
		}

		if (StringUtils.hasLength(agentPath)) {
			String normalizedAgentPath = StringUtils.trimTrailingCharacter(agentPath, '/');
			normalizedAgentPath = StringUtils.trimLeadingCharacter(normalizedAgentPath, '/');

			builder.setPath(normalizedAgentPath);
		}

		return new ConsulClient(builder.build());
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass({ Endpoint.class, Health.class })
	@EnableConfigurationProperties(ConsulHealthIndicatorProperties.class)
	protected static class ConsulHealthConfig {

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnAvailableEndpoint
		public ConsulEndpoint consulEndpoint(ConsulClient consulClient) {
			return new ConsulEndpoint(consulClient);
		}

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnEnabledHealthIndicator("consul")
		public ConsulHealthIndicator consulHealthIndicator(ConsulClient consulClient,
				ConsulHealthIndicatorProperties properties) {
			return new ConsulHealthIndicator(consulClient, properties);
		}

	}

	@ConditionalOnClass({ Retryable.class, Aspect.class, AopAutoConfiguration.class })
	@Configuration(proxyBeanMethods = false)
	@EnableRetry(proxyTargetClass = true)
	@Import(AopAutoConfiguration.class)
	@EnableConfigurationProperties(RetryProperties.class)
	@ConditionalOnProperty(value = "spring.cloud.consul.retry.enabled", matchIfMissing = true)
	protected static class RetryConfiguration {

		@Bean(name = "consulRetryInterceptor")
		@ConditionalOnMissingBean(name = "consulRetryInterceptor")
		public RetryOperationsInterceptor consulRetryInterceptor(RetryProperties properties) {
			return RetryInterceptorBuilder.stateless()
				.backOffOptions(properties.getInitialInterval(), properties.getMultiplier(),
						properties.getMaxInterval())
				.maxAttempts(properties.getMaxAttempts())
				.build();
		}

	}

}
