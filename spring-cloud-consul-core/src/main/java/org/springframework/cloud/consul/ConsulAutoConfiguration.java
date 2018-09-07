/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.consul;

import com.ecwid.consul.transport.TLSConfig;
import com.ecwid.consul.v1.ConsulClient;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnEnabledEndpoint;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.util.StringUtils;

/**
 * @author Spencer Gibb
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnConsulEnabled
public class ConsulAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public ConsulProperties consulProperties() {
		return new ConsulProperties();
	}

	@Bean
	@ConditionalOnMissingBean
	public ConsulClient consulClient(ConsulProperties consulProperties) {
		final int agentPort = consulProperties.getPort();
		final String agentHost = !StringUtils.isEmpty(consulProperties.getScheme())
				? consulProperties.getScheme() + "://" + consulProperties.getHost()
				: consulProperties.getHost();

		if (consulProperties.getTls() != null) {
			ConsulProperties.TLSConfig tls = consulProperties.getTls();
			TLSConfig tlsConfig = new TLSConfig(
					tls.getKeyStoreInstanceType(),
					tls.getCertificatePath(),
					tls.getCertificatePassword(),
					tls.getKeyStorePath(),
					tls.getKeyStorePassword()
			);
			return new ConsulClient(agentHost, agentPort, tlsConfig);
		}
		return new ConsulClient(agentHost, agentPort);
	}

	@Configuration
	@ConditionalOnClass(Endpoint.class)
	protected static class ConsulHealthConfig {

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnEnabledEndpoint
		public ConsulEndpoint consulEndpoint(ConsulClient consulClient) {
			return new ConsulEndpoint(consulClient);
		}

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnEnabledHealthIndicator("consul")
		public ConsulHealthIndicator consulHealthIndicator(ConsulClient consulClient) {
			return new ConsulHealthIndicator(consulClient);
		}
	}

	@ConditionalOnClass({ Retryable.class, Aspect.class, AopAutoConfiguration.class })
	@Configuration
	@EnableRetry(proxyTargetClass = true)
	@Import(AopAutoConfiguration.class)
	@EnableConfigurationProperties(RetryProperties.class)
	protected static class RetryConfiguration {

		@Bean(name = "consulRetryInterceptor")
		@ConditionalOnMissingBean(name = "consulRetryInterceptor")
		public RetryOperationsInterceptor consulRetryInterceptor(
				RetryProperties properties) {
			return RetryInterceptorBuilder
					.stateless()
					.backOffOptions(properties.getInitialInterval(),
							properties.getMultiplier(), properties.getMaxInterval())
					.maxAttempts(properties.getMaxAttempts()).build();
		}
	}
}
