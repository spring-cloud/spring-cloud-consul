/*
 * Copyright 2013-2017 the original author or authors.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ecwid.consul.v1.agent.model.NewService;

import org.springframework.cloud.client.discovery.ManagementServerPortUtils;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.commons.util.IdUtils;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author Spencer Gibb
 */
public class ConsulAutoRegistration extends ConsulRegistration {

	/**
	 * Instance ID separator.
	 */
	public static final char SEPARATOR = '-';

	private final AutoServiceRegistrationProperties autoServiceRegistrationProperties;

	private final ApplicationContext context;

	private final HeartbeatProperties heartbeatProperties;

	private final List<ConsulManagementRegistrationCustomizer> managementRegistrationCustomizers;

	@Deprecated
	public ConsulAutoRegistration(NewService service,
			AutoServiceRegistrationProperties autoServiceRegistrationProperties, ConsulDiscoveryProperties properties,
			ApplicationContext context, HeartbeatProperties heartbeatProperties) {
		this(service, autoServiceRegistrationProperties, properties, context, heartbeatProperties,
				Collections.emptyList());
	}

	public ConsulAutoRegistration(NewService service,
			AutoServiceRegistrationProperties autoServiceRegistrationProperties, ConsulDiscoveryProperties properties,
			ApplicationContext context, HeartbeatProperties heartbeatProperties,
			List<ConsulManagementRegistrationCustomizer> managementRegistrationCustomizers) {
		super(service, properties);
		this.autoServiceRegistrationProperties = autoServiceRegistrationProperties;
		this.context = context;
		this.heartbeatProperties = heartbeatProperties;
		this.managementRegistrationCustomizers = managementRegistrationCustomizers;
	}

	public static ConsulAutoRegistration registration(
			AutoServiceRegistrationProperties autoServiceRegistrationProperties, ConsulDiscoveryProperties properties,
			ApplicationContext context, List<ConsulRegistrationCustomizer> registrationCustomizers,
			List<ConsulManagementRegistrationCustomizer> managementRegistrationCustomizers,
			HeartbeatProperties heartbeatProperties) {

		NewService service = new NewService();
		String appName = getAppName(properties, context.getEnvironment());
		service.setId(getInstanceId(properties, context));
		if (!properties.isPreferAgentAddress()) {
			service.setAddress(properties.getHostname());
		}
		service.setName(normalizeForDns(appName));
		service.setTags(new ArrayList<>(properties.getTags()));
		service.setEnableTagOverride(properties.getEnableTagOverride());
		service.setMeta(getMetadata(properties));

		if (properties.getPort() != null) {
			service.setPort(properties.getPort());
			// we know the port and can set the check
			setCheck(service, autoServiceRegistrationProperties, properties, context, heartbeatProperties);
		}

		ConsulAutoRegistration registration = new ConsulAutoRegistration(service, autoServiceRegistrationProperties,
				properties, context, heartbeatProperties, managementRegistrationCustomizers);
		customize(registrationCustomizers, registration);
		return registration;
	}

	public static void customize(List<ConsulRegistrationCustomizer> registrationCustomizers,
			ConsulAutoRegistration registration) {
		if (registrationCustomizers != null) {
			for (ConsulRegistrationCustomizer customizer : registrationCustomizers) {
				customizer.customize(registration);
			}
		}
	}

	public static void setCheck(NewService service, AutoServiceRegistrationProperties autoServiceRegistrationProperties,
			ConsulDiscoveryProperties properties, ApplicationContext context, HeartbeatProperties heartbeatProperties) {
		if (properties.isRegisterHealthCheck() && service.getCheck() == null) {
			Integer checkPort;
			if (shouldRegisterManagement(autoServiceRegistrationProperties, properties, context)) {
				checkPort = getManagementPort(properties, context);
			}
			else {
				checkPort = service.getPort();
			}
			Assert.notNull(checkPort, "checkPort may not be null");
			service.setCheck(createCheck(checkPort, heartbeatProperties, properties));
		}
	}

	public static ConsulAutoRegistration managementRegistration(
			AutoServiceRegistrationProperties autoServiceRegistrationProperties, ConsulDiscoveryProperties properties,
			ApplicationContext context, List<ConsulManagementRegistrationCustomizer> managementRegistrationCustomizers,
			HeartbeatProperties heartbeatProperties) {
		NewService management = new NewService();
		management.setId(getManagementServiceId(properties, context));
		management.setAddress(properties.getHostname());
		management.setName(getManagementServiceName(properties, context.getEnvironment()));
		management.setPort(getManagementPort(properties, context));
		management.setTags(properties.getManagementTags());
		management.setEnableTagOverride(properties.getManagementEnableTagOverride());
		management.setMeta(properties.getManagementMetadata());
		if (properties.isRegisterHealthCheck()) {
			management.setCheck(createCheck(getManagementPort(properties, context), heartbeatProperties, properties));
		}
		ConsulAutoRegistration registration = new ConsulAutoRegistration(management, autoServiceRegistrationProperties,
				properties, context, heartbeatProperties, managementRegistrationCustomizers);
		managementCustomize(managementRegistrationCustomizers, registration);
		return registration;
	}

	public static void managementCustomize(List<ConsulManagementRegistrationCustomizer> registrationCustomizers,
			ConsulAutoRegistration registration) {
		if (registrationCustomizers != null) {
			for (ConsulManagementRegistrationCustomizer customizer : registrationCustomizers) {
				customizer.customize(registration);
			}
		}
	}

	public static String getInstanceId(ConsulDiscoveryProperties properties, ApplicationContext context) {
		if (!StringUtils.hasText(properties.getInstanceId())) {
			return normalizeForDns(
					IdUtils.getDefaultInstanceId(context.getEnvironment(), properties.isIncludeHostnameInInstanceId()));
		}
		return normalizeForDns(properties.getInstanceId());
	}

	public static String normalizeForDns(String s) {
		if (s == null || !Character.isLetter(s.charAt(0)) || !Character.isLetterOrDigit(s.charAt(s.length() - 1))) {
			throw new IllegalArgumentException(
					"Consul service ids must not be empty, must start " + "with a letter, end with a letter or digit, "
							+ "and have as interior characters only letters, " + "digits, and hyphen: " + s);
		}

		StringBuilder normalized = new StringBuilder();
		Character prev = null;
		for (char curr : s.toCharArray()) {
			Character toAppend = null;
			if (Character.isLetterOrDigit(curr)) {
				toAppend = curr;
			}
			else if (prev == null || !(prev == SEPARATOR)) {
				toAppend = SEPARATOR;
			}
			if (toAppend != null) {
				normalized.append(toAppend);
				prev = toAppend;
			}
		}

		return normalized.toString();
	}

	private static Map<String, String> getMetadata(ConsulDiscoveryProperties properties) {
		LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
		if (!CollectionUtils.isEmpty(properties.getMetadata())) {
			metadata.putAll(properties.getMetadata());
		}

		// add metadata from other properties. See createTags above.
		if (!StringUtils.isEmpty(properties.getInstanceZone())) {
			metadata.put(properties.getDefaultZoneMetadataName(), properties.getInstanceZone());
		}
		if (!StringUtils.isEmpty(properties.getInstanceGroup())) {
			metadata.put("group", properties.getInstanceGroup());
		}

		// store the secure flag in the tags so that clients will be able to figure
		// out whether to use http or https automatically
		metadata.put("secure", Boolean.toString(properties.getScheme().equalsIgnoreCase("https")));

		return metadata;
	}

	public static NewService.Check createCheck(Integer port, HeartbeatProperties ttlConfig,
			ConsulDiscoveryProperties properties) {
		NewService.Check check = new NewService.Check();
		if (StringUtils.hasText(properties.getHealthCheckCriticalTimeout())) {
			check.setDeregisterCriticalServiceAfter(properties.getHealthCheckCriticalTimeout());
		}
		if (ttlConfig.isEnabled()) {
			// FIXME 3.0.0
			// https://github.com/spring-cloud/spring-cloud-consul/issues/614
			check.setTtl(ttlConfig.getTtl().getSeconds() + "s");
			return check;
		}

		Assert.notNull(port, "createCheck port must not be null");
		Assert.isTrue(port > 0, "createCheck port must be greater than 0");

		if (properties.getHealthCheckUrl() != null) {
			check.setHttp(properties.getHealthCheckUrl());
		}
		else {
			check.setHttp(String.format("%s://%s:%s%s", properties.getScheme(), properties.getHostname(), port,
					properties.getHealthCheckPath()));
		}
		check.setHeader(properties.getHealthCheckHeaders());
		check.setInterval(properties.getHealthCheckInterval());
		check.setTimeout(properties.getHealthCheckTimeout());
		check.setTlsSkipVerify(properties.getHealthCheckTlsSkipVerify());
		return check;
	}

	/**
	 * @param properties consul discovery properties
	 * @param env Spring environment
	 * @return the app name, currently the spring.application.name property
	 */
	public static String getAppName(ConsulDiscoveryProperties properties, Environment env) {
		final String appName = properties.getServiceName();
		if (StringUtils.hasText(appName)) {
			return appName;
		}
		return env.getProperty("spring.application.name", "application");
	}

	/**
	 * @param autoServiceRegistrationProperties registration properties
	 * @param properties discovery properties
	 * @param context Spring application context
	 * @return if the management service should be registered with the
	 * {@link ServiceRegistry}
	 */
	public static boolean shouldRegisterManagement(AutoServiceRegistrationProperties autoServiceRegistrationProperties,
			ConsulDiscoveryProperties properties, ApplicationContext context) {
		return autoServiceRegistrationProperties.isRegisterManagement()
				&& getManagementPort(properties, context) != null && ManagementServerPortUtils.isDifferent(context);
	}

	/**
	 * @param properties discovery properties
	 * @param context Spring application context
	 * @return the serviceId of the Management Service
	 */
	public static String getManagementServiceId(ConsulDiscoveryProperties properties, ApplicationContext context) {
		final String instanceId = properties.getInstanceId();
		if (StringUtils.hasText(instanceId)) {
			return normalizeForDns(instanceId + SEPARATOR + properties.getManagementSuffix());
		}
		return normalizeForDns(IdUtils.getDefaultInstanceId(context.getEnvironment(), false)) + SEPARATOR
				+ properties.getManagementSuffix();
	}

	/**
	 * @param properties discovery properties
	 * @param env Spring environment
	 * @return the service name of the Management Service
	 */
	public static String getManagementServiceName(ConsulDiscoveryProperties properties, Environment env) {
		final String appName = properties.getServiceName();
		if (StringUtils.hasText(appName)) {
			return normalizeForDns(appName + SEPARATOR + properties.getManagementSuffix());
		}
		return normalizeForDns(getAppName(properties, env)) + SEPARATOR + properties.getManagementSuffix();
	}

	/**
	 * @param properties discovery properties
	 * @param context Spring application context
	 * @return the port of the Management Service
	 */
	public static Integer getManagementPort(ConsulDiscoveryProperties properties, ApplicationContext context) {
		// If an alternate external port is specified, use it instead
		if (properties.getManagementPort() != null) {
			return properties.getManagementPort();
		}
		return ManagementServerPortUtils.getPort(context);
	}

	public void initializePort(int knownPort) {
		if (getService().getPort() == null) {
			// not set by properties
			getService().setPort(knownPort);
		}
		// we might not have a port until now, so this is the earliest we
		// can create a check

		setCheck(getService(), this.autoServiceRegistrationProperties, getProperties(), this.context,
				this.heartbeatProperties);
	}

	public ConsulAutoRegistration managementRegistration() {
		return managementRegistration(this.autoServiceRegistrationProperties, getProperties(), this.context,
				this.managementRegistrationCustomizers, this.heartbeatProperties);
	}

}
