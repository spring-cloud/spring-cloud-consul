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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.function.Supplier;

import com.ecwid.consul.transport.TLSConfig;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.ConsulRawClient;
import com.ecwid.consul.v1.ConsulRawClient.Builder;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslStoreBundle;
import org.springframework.cloud.consul.model.http.KeyStoreInstanceType;
import org.springframework.cloud.consul.model.http.format.WaitTimeAnnotationFormatterFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.ConversionService;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpExchangeAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;

/**
 * @author Spencer Gibb
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnConsulEnabled
public class ConsulAutoConfiguration {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConsulAutoConfiguration.class);

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

	@Bean
	@ConditionalOnMissingBean
	public IConsulClient newConsulClient(ConsulProperties consulProperties)
			throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
		return createNewConsulClient(consulProperties);
	}

	public static IConsulClient createNewConsulClient(ConsulProperties consulProperties)
			throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
		UriBuilder uriBuilder = new DefaultUriBuilderFactory().builder();

		if (StringUtils.hasLength(consulProperties.getScheme())) {
			uriBuilder.scheme(consulProperties.getScheme());
		}
		else {
			uriBuilder.scheme("http");
		}

		uriBuilder.host(consulProperties.getHost()).port(consulProperties.getPort());

		final String agentPath = consulProperties.getPath();
		if (StringUtils.hasLength(agentPath)) {
			String normalizedAgentPath = StringUtils.trimTrailingCharacter(agentPath, '/');
			normalizedAgentPath = StringUtils.trimLeadingCharacter(normalizedAgentPath, '/');

			uriBuilder.path(normalizedAgentPath);
		}

		String baseUrl = uriBuilder.build().toString();

		HttpExchangeAdapter adapter = createConsulRestClientAdapter(baseUrl, consulProperties.getTls());
		HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter)
			.conversionService(createConsulClientConversionService())
			.build();

		return factory.createClient(IConsulClient.class);
	}

	public static RestClientAdapter createConsulRestClientAdapter(String baseUrl, ConsulProperties.TLSConfig tlsConfig)
			throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
		RestClient.Builder builder = RestClient.builder()
			.defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
			})
			.defaultStatusHandler(HttpStatusCode::is5xxServerError,
					(request, response) -> LOGGER
						.error(new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8)))
			.baseUrl(baseUrl);

		if (tlsConfig != null) {
			KeyStore clientStore = KeyStore.getInstance(tlsConfig.getKeyStoreInstanceType().name());
			clientStore.load(Files.newInputStream(Paths.get(tlsConfig.getCertificatePath())),
					tlsConfig.getCertificatePassword().toCharArray());

			KeyStore trustStore = KeyStore.getInstance(KeyStoreInstanceType.JKS.name());
			trustStore.load(Files.newInputStream(Paths.get(tlsConfig.getKeyStorePath())),
					tlsConfig.getKeyStorePassword().toCharArray());

			SslStoreBundle sslStoreBundle = SslStoreBundle.of(clientStore, tlsConfig.getKeyStorePassword(), trustStore);
			SslBundle sslBundle = SslBundle.of(sslStoreBundle);
			ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.ofSslBundle(sslBundle);
			ClientHttpRequestFactory requestFactory = ClientHttpRequestFactoryBuilder.detect().build(settings);
			builder.requestFactory(requestFactory);
		}

		return RestClientAdapter.create(builder.build());
	}

	public static ConversionService createConsulClientConversionService() {
		DefaultFormattingConversionService conversionService = new DefaultFormattingConversionService();
		conversionService.addFormatterForFieldAnnotation(new WaitTimeAnnotationFormatterFactory());
		return conversionService;
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
			TLSConfig.KeyStoreInstanceType keyStoreInstanceType = TLSConfig.KeyStoreInstanceType
				.valueOf(tls.getKeyStoreInstanceType().toString());
			TLSConfig tlsConfig = new TLSConfig(keyStoreInstanceType, tls.getCertificatePath(),
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
		public ConsulEndpoint consulEndpoint(IConsulClient consulClient) {
			return new ConsulEndpoint(consulClient);
		}

		@Bean
		@ConditionalOnMissingBean
		@ConditionalOnEnabledHealthIndicator("consul")
		public ConsulHealthIndicator consulHealthIndicator(IConsulClient consulClient,
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
