/*
 * Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.consul.serviceregistry;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;

import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.cloud.client.discovery.ManagementServerPortUtils;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.ecwid.consul.v1.agent.model.NewService;

/**
 * @author Spencer Gibb
 */
public class ConsulRegistration implements Registration {

	public static final char SEPARATOR = '-';

	private final NewService service;
	private final ConsulDiscoveryProperties properties;
	private final ApplicationContext context;
	private final HeartbeatProperties heartbeatProperties;
	private String instanceId;

	public ConsulRegistration(NewService service, ConsulDiscoveryProperties properties, ApplicationContext context, HeartbeatProperties heartbeatProperties) {
		this.service = service;
		this.properties = properties;
		this.context = context;
		this.heartbeatProperties = heartbeatProperties;

		// cache instanceId, so on refresh this won't get recomputed
		// this is a problem if ${random.value} is used
		this.instanceId = ConsulRegistration.getServiceId(properties, context);
	}

	public String getInstanceId() {
		return this.instanceId;
	}

	public void initializePort(int knownPort) {
		if (getService().getPort() == null) {
			// not set by properties
			getService().setPort(knownPort);
		}
		// we might not have a port until now, so this is the earliest we
		// can create a check

		setCheck(this.service, this.properties, this.context, this.heartbeatProperties);
	}

	public ConsulRegistration managementRegistration() {
		return managementRegistration(this.properties, this.context, this.heartbeatProperties);
	}

	public static ConsulRegistration registration(ConsulDiscoveryProperties properties, ApplicationContext context,
												  ServletContext servletContext, HeartbeatProperties heartbeatProperties) {
		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(context.getEnvironment());

		NewService service = new NewService();
		String appName = getAppName(properties, propertyResolver);
		service.setId(getServiceId(properties, context));
		if(!properties.isPreferAgentAddress()) {
			service.setAddress(properties.getHostname());
		}
		service.setName(normalizeForDns(appName));
		service.setTags(createTags(properties, servletContext));

		if (properties.getPort() != null) {
			service.setPort(properties.getPort());
		}

		return new ConsulRegistration(service, properties, context, heartbeatProperties);
	}

	@Deprecated //TODO: do I need this here, or should I just copy what I need back into lifecycle?
	public static ConsulRegistration lifecycleRegistration(Integer port, ConsulDiscoveryProperties properties, ApplicationContext context,
												  ServletContext servletContext, HeartbeatProperties heartbeatProperties) {
		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(context.getEnvironment());

		NewService service = new NewService();
		String appName = getAppName(properties, propertyResolver);
		service.setId(getServiceId(properties, context));
		if(!properties.isPreferAgentAddress()) {
			service.setAddress(properties.getHostname());
		}
		service.setName(normalizeForDns(appName));
		service.setTags(createTags(properties, servletContext));

		// If an alternate external port is specified, register using it instead
		if (properties.getPort() != null) {
			service.setPort(properties.getPort());
		} else {
			service.setPort(port);
		}

		Assert.notNull(service.getPort(), "service.port may not be null");

		setCheck(service, properties, context, heartbeatProperties);

		return new ConsulRegistration(service, properties, context, heartbeatProperties);
	}

	public static void setCheck(NewService service, ConsulDiscoveryProperties properties, ApplicationContext context, HeartbeatProperties heartbeatProperties) {
		if (properties.isRegisterHealthCheck()) {
			Integer checkPort;
			if (shouldRegisterManagement(properties, context)) {
				checkPort = getManagementPort(properties, context);
			} else {
				checkPort = service.getPort();
			}
			Assert.notNull(checkPort, "checkPort may not be null");
			service.setCheck(createCheck(checkPort, heartbeatProperties, properties));
		}
	}

	public static ConsulRegistration managementRegistration(ConsulDiscoveryProperties properties, ApplicationContext context,
													 HeartbeatProperties heartbeatProperties) {
		RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(context.getEnvironment());
		NewService management = new NewService();
		management.setId(getManagementServiceId(properties, context));
		management.setAddress(properties.getHostname());
		management.setName(getManagementServiceName(properties, propertyResolver));
		management.setPort(getManagementPort(properties, context));
		management.setTags(properties.getManagementTags());
		if (properties.isRegisterHealthCheck()) {
			management.setCheck(createCheck(getManagementPort(properties, context), heartbeatProperties, properties));
		}
		return new ConsulRegistration(management, properties, context, heartbeatProperties);
	}

	public String getServiceId() {
		return this.service.getId();
	}

	public static String getServiceId(ConsulDiscoveryProperties properties, ApplicationContext context) {
		if (!StringUtils.hasText(properties.getInstanceId())) {
			return normalizeForDns(context.getId());
		} else {
			return normalizeForDns(properties.getInstanceId());
		}
	}

	public static String normalizeForDns(String s) {
		if (s == null || !Character.isLetter(s.charAt(0))
				|| !Character.isLetterOrDigit(s.charAt(s.length()-1))) {
			throw new IllegalArgumentException("Consul service ids must not be empty, must start with a letter, end with a letter or digit, and have as interior characters only letters, digits, and hyphen");
		}

		StringBuilder normalized = new StringBuilder();
		Character prev = null;
		for (char curr : s.toCharArray()) {
			Character toAppend = null;
			if (Character.isLetterOrDigit(curr)) {
				toAppend = curr;
			} else if (prev == null || !(prev == SEPARATOR)) {
				toAppend = SEPARATOR;
			}
			if (toAppend != null) {
				normalized.append(toAppend);
				prev = toAppend;
			}
		}

		return normalized.toString();
	}


	public static List<String> createTags(ConsulDiscoveryProperties properties, ServletContext servletContext) {
		List<String> tags = new LinkedList<>(properties.getTags());
		if(servletContext != null
				&& StringUtils.hasText(servletContext.getContextPath())
				&& StringUtils.hasText(servletContext.getContextPath().replaceAll("/", ""))) {
			tags.add("contextPath=" + servletContext.getContextPath());
		}
		if (!StringUtils.isEmpty(properties.getInstanceZone())) {
			tags.add(properties.getDefaultZoneMetadataName() + "=" + properties.getInstanceZone());
		}
		if (!StringUtils.isEmpty(properties.getInstanceGroup())) {
			tags.add("group=" + properties.getInstanceGroup());
		}
		return tags;
	}

	public static NewService.Check createCheck(Integer port, HeartbeatProperties ttlConfig,
										 ConsulDiscoveryProperties properties) {
		NewService.Check check = new NewService.Check();
		if (ttlConfig.isEnabled()) {
			check.setTtl(ttlConfig.getTtl());
			return check;
		}

		Assert.notNull(port, "createCheck port must not be null");
		Assert.isTrue(port > 0, "createCheck port must be greater than 0");

		if (properties.getHealthCheckUrl() != null) {
			check.setHttp(properties.getHealthCheckUrl());
		} else {
			check.setHttp(String.format("%s://%s:%s%s", properties.getScheme(),
					properties.getHostname(), port,
					properties.getHealthCheckPath()));
		}
		check.setInterval(properties.getHealthCheckInterval());
		check.setTimeout(properties.getHealthCheckTimeout());
		return check;
	}

	/**
	 * @return the app name, currently the spring.application.name property
	 */
	public static String getAppName(ConsulDiscoveryProperties properties, RelaxedPropertyResolver propertyResolver) {
		String appName = properties.getServiceName();
		if (!StringUtils.isEmpty(appName)) {
			return appName;
		}
		return propertyResolver.getProperty("spring.application.name", "application");
	}

	/**
	 * @return if the management service should be registered with the {@link ServiceRegistry}
	 */
	public static boolean shouldRegisterManagement(ConsulDiscoveryProperties properties, ApplicationContext context) {
		return getManagementPort(properties, context) != null && ManagementServerPortUtils.isDifferent(context);
	}

	/**
	 * @return the serviceId of the Management Service
	 */
	public static String getManagementServiceId(ConsulDiscoveryProperties properties, ApplicationContext context) {
		return normalizeForDns(context.getId()) + SEPARATOR + properties.getManagementSuffix();
	}

	/**
	 * @return the service name of the Management Service
	 */
	public static String getManagementServiceName(ConsulDiscoveryProperties properties, RelaxedPropertyResolver propertyResolver) {
		return normalizeForDns(getAppName(properties, propertyResolver)) + SEPARATOR + properties.getManagementSuffix();
	}

	/**
	 * @return the port of the Management Service
	 */
	public static Integer getManagementPort(ConsulDiscoveryProperties properties, ApplicationContext context) {
		// If an alternate external port is specified, use it instead
		if (properties.getManagementPort() != null) {
			return properties.getManagementPort();
		}
		return ManagementServerPortUtils.getPort(context);
	}

	public NewService getService() {
		return service;
	}
}
