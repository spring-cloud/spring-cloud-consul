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

package org.springframework.cloud.consul.discovery;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.AbstractDiscoveryLifecycle;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;

/**
 * @author Spencer Gibb
 */
@Slf4j
public class ConsulLifecycle extends AbstractDiscoveryLifecycle {

	public static final char SEPARATOR = '-';

	@Autowired
	private ConsulClient client;

	@Autowired
	private ConsulDiscoveryProperties properties;

	@Autowired(required = false)
	private TtlScheduler ttlScheduler;

	@Autowired
	private HeartbeatProperties ttlConfig;

	@Autowired(required = false)
	private ServletContext servletContext;
	
	private NewService service = new NewService();

	@Override
	protected int getConfiguredPort() {
		return service.getPort() == null? 0 : service.getPort();
	}

	@Override
	protected void setConfiguredPort(int port) {
		service.setPort(port);
	}

	@Override
	protected void register() {
		Assert.notNull(service.getPort(), "service.port has not been set");
		String appName = getAppName();
		service.setId(getServiceId());
		service.setName(normalizeForDns(appName));
		service.setTags(createTags());

		Integer port;
		if (shouldRegisterManagement()) {
			port = getManagementPort();
		} else {
			port = service.getPort();
		}
		service.setCheck(createCheck(port));

		register(service);
	}

	private NewService.Check createCheck(Integer port) {
		NewService.Check check = new NewService.Check();
		if (ttlConfig.isEnabled()) {
			check.setTtl(ttlConfig.getTtl());
			return check;
		}

		if (properties.getHealthCheckUrl() != null) {
			check.setHttp(properties.getHealthCheckUrl());
		} else {
			check.setHttp(String.format("%s://%s:%s%s", properties.getScheme(),
					properties.getHostname(), port,
					properties.getHealthCheckPath()));
		}
		check.setInterval(properties.getHealthCheckInterval());
		//TODO support http check timeout
		return check;
	}

	public String getServiceId() {
		if (!StringUtils.hasText(properties.getInstanceId())) {
			return normalizeForDns(getContext().getId());
		} else {
			return normalizeForDns(properties.getInstanceId());
		}
	}

	@Override
	protected void registerManagement() {
		NewService management = new NewService();
		management.setId(getManagementServiceId());
		management.setName(getManagementServiceName());
		management.setPort(getManagementPort());
		management.setTags(properties.getManagementTags());
		management.setCheck(createCheck(getManagementPort()));

		register(management);
	}

	protected void register(NewService newService) {
		log.info("Registering service with consul: {}", newService.toString());
		if (properties.getAclToken() == null) {
			client.agentServiceRegister(newService);
		} else {
			client.agentServiceRegister(newService, properties.getAclToken());
		}
		if (ttlConfig.isEnabled() && ttlScheduler != null) {
			ttlScheduler.add(newService);
		}
	}

	@Override
	protected Object getConfiguration() {
		return properties;
	}

	@Override
	protected void deregister() {
		deregister(getServiceId());
	}

	@Override
	protected void deregisterManagement() {
		deregister(getManagementServiceId());
	}

	private List<String> createTags() {
		List<String> tags = new LinkedList<>(properties.getTags());
		if(servletContext != null
				&& StringUtils.hasText(servletContext.getContextPath())
				&& StringUtils.hasText(servletContext.getContextPath().replaceAll("/", ""))) {
			tags.add("contextPath=" + servletContext.getContextPath());
		}
		return tags;
	}

	private void deregister(String serviceId) {
		if (ttlScheduler != null) {
			ttlScheduler.remove(serviceId);
		}
		client.agentServiceDeregister(serviceId);
	}

	@Override
	protected boolean isEnabled() {
		return properties.isEnabled();
	}

	/**
	 * @return the serviceId of the Management Service
	 */
	public String getManagementServiceId() {
		return normalizeForDns(getContext().getId()) + SEPARATOR + properties.getManagementSuffix();
	}

	/**
	 * @return the service name of the Management Service
	 */
	public String getManagementServiceName() {
		return normalizeForDns(getAppName()) + SEPARATOR + properties.getManagementSuffix();
	}

	public static String normalizeForDns(String s) {
		if (!Character.isLetter(s.charAt(0))
				|| !Character.isLetterOrDigit(s.charAt(s.length()-1))) {
			throw new IllegalArgumentException("Consul service ids must start with a letter, end with a letter or digit, and have as interior characters only letters, digits, and hyphen");
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
}
