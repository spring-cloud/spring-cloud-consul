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

import java.util.function.Supplier;

import com.ecwid.consul.transport.AbstractHttpTransport;
import com.ecwid.consul.transport.HttpResponse;
import com.ecwid.consul.transport.TLSConfig;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.ConsulRawClient;
import com.ecwid.consul.v1.ConsulRawClient.Builder;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.acl.AclClient;
import com.ecwid.consul.v1.acl.model.NewAcl;
import com.ecwid.consul.v1.acl.model.UpdateAcl;
import com.ecwid.consul.v1.agent.AgentClient;
import com.ecwid.consul.v1.agent.AgentConsulClient;
import com.ecwid.consul.v1.agent.model.Check;
import com.ecwid.consul.v1.agent.model.NewCheck;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.catalog.CatalogClient;
import com.ecwid.consul.v1.catalog.CatalogConsulClient;
import com.ecwid.consul.v1.catalog.CatalogNodesRequest;
import com.ecwid.consul.v1.catalog.CatalogServiceRequest;
import com.ecwid.consul.v1.catalog.model.CatalogDeregistration;
import com.ecwid.consul.v1.catalog.model.CatalogRegistration;
import com.ecwid.consul.v1.coordinate.CoordinateClient;
import com.ecwid.consul.v1.event.EventClient;
import com.ecwid.consul.v1.event.EventListRequest;
import com.ecwid.consul.v1.event.model.Event;
import com.ecwid.consul.v1.event.model.EventParams;
import com.ecwid.consul.v1.health.HealthChecksForServiceRequest;
import com.ecwid.consul.v1.health.HealthClient;
import com.ecwid.consul.v1.health.HealthServicesRequest;
import com.ecwid.consul.v1.health.model.HealthService;
import com.ecwid.consul.v1.kv.KeyValueClient;
import com.ecwid.consul.v1.kv.model.GetBinaryValue;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.ecwid.consul.v1.kv.model.PutParams;
import com.ecwid.consul.v1.query.QueryClient;
import com.ecwid.consul.v1.session.SessionClient;
import com.ecwid.consul.v1.session.model.Session;
import com.ecwid.consul.v1.status.StatusClient;
import org.aspectj.lang.annotation.Aspect;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.util.ClassUtils;
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
	@ConditionalOnMissingBean
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
	@ConditionalOnClass(Endpoint.class)
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
			return RetryInterceptorBuilder.stateless().backOffOptions(properties.getInitialInterval(),
					properties.getMultiplier(), properties.getMaxInterval()).maxAttempts(properties.getMaxAttempts())
					.build();
		}

	}

}

class ConsulHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		if (!ClassUtils.isPresent("com.ecwid.consul.v1.ConsulClient", classLoader)) {
			return;
		}
		hints.reflection().registerType(TypeReference.of(NewService.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(NewAcl.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(UpdateAcl.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(NewCheck.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(CatalogDeregistration.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(CatalogRegistration.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(CatalogRegistration.Service.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(QueryParams.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(EventParams.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(EventListRequest.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(CatalogNodesRequest.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(CatalogServiceRequest.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(HealthChecksForServiceRequest.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(HealthServicesRequest.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(PutParams.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(AbstractHttpTransport.class),
				hint -> hint.withMembers(MemberCategory.INTROSPECT_DECLARED_METHODS));
		hints.reflection().registerType(TypeReference.of(ConsulClient.class),
				hint -> hint.withMembers(MemberCategory.INTROSPECT_DECLARED_METHODS));
		hints.reflection().registerType(TypeReference.of(ConsulRawClient.class),
				hint -> hint.withMembers(MemberCategory.INTROSPECT_DECLARED_METHODS));
		hints.reflection().registerType(TypeReference.of(AgentConsulClient.class),
				hint -> hint.withMembers(MemberCategory.INTROSPECT_DECLARED_METHODS));
		hints.reflection().registerType(TypeReference.of(CatalogConsulClient.class),
				hint -> hint.withMembers(MemberCategory.INTROSPECT_DECLARED_METHODS));
		hints.reflection().registerType(TypeReference.of(AclClient.class),
				hint -> hint.withMembers(MemberCategory.INTROSPECT_DECLARED_METHODS));
		hints.reflection().registerType(TypeReference.of(AgentClient.class),
				hint -> hint.withMembers(MemberCategory.INTROSPECT_DECLARED_METHODS));
		hints.reflection().registerType(TypeReference.of(CatalogClient.class),
				hint -> hint.withMembers(MemberCategory.INTROSPECT_DECLARED_METHODS));
		hints.reflection().registerType(TypeReference.of(CoordinateClient.class),
				hint -> hint.withMembers(MemberCategory.INTROSPECT_DECLARED_METHODS));
		hints.reflection().registerType(TypeReference.of(EventClient.class),
				hint -> hint.withMembers(MemberCategory.INTROSPECT_DECLARED_METHODS));
		hints.reflection().registerType(TypeReference.of(HealthClient.class),
				hint -> hint.withMembers(MemberCategory.INTROSPECT_DECLARED_METHODS));
		hints.reflection().registerType(TypeReference.of(KeyValueClient.class),
				hint -> hint.withMembers(MemberCategory.INTROSPECT_DECLARED_METHODS));
		hints.reflection().registerType(TypeReference.of(QueryClient.class),
				hint -> hint.withMembers(MemberCategory.INTROSPECT_DECLARED_METHODS));
		hints.reflection().registerType(TypeReference.of(SessionClient.class),
				hint -> hint.withMembers(MemberCategory.INTROSPECT_DECLARED_METHODS));
		hints.reflection().registerType(TypeReference.of(StatusClient.class),
				hint -> hint.withMembers(MemberCategory.INTROSPECT_DECLARED_METHODS));
		hints.reflection().registerType(TypeReference.of(HttpResponse.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(HealthService.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(HealthService.Node.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(HealthService.Service.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(Response.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(Session.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(GetBinaryValue.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(GetValue.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(Check.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(Check.CheckStatus.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(com.ecwid.consul.v1.health.model.Check.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
		hints.reflection().registerType(TypeReference.of(Event.class),
				hint -> hint.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS));
	}

}
