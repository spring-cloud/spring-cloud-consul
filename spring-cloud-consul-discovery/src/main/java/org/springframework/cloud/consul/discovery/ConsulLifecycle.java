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

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.AbstractDiscoveryLifecycle;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import org.springframework.util.Assert;

import javax.servlet.ServletContext;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Spencer Gibb
 */
@Slf4j
public class ConsulLifecycle extends AbstractDiscoveryLifecycle {

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
		String id;
		if (properties.getInstanceId() == null) {
			id = getContext().getId();
		} else {
			id = properties.getInstanceId();
		}
		service.setId(id);
		service.setName(appName);
		service.setTags(createTags());

		NewService.Check check = new NewService.Check();
		if (ttlConfig.isEnabled()) {
			check.setTtl(ttlConfig.getTtl());
		}
		if (properties.getHealthCheckUrl() != null) {
			check.setHttp(properties.getHealthCheckUrl());
		} else {
			check.setHttp(String.format("%s://%s:%s%s", properties.getScheme(),
					properties.getHostname(), service.getPort(),
					properties.getHealthCheckPath()));
		}
		check.setInterval(properties.getHealthCheckInterval());
		//TODO support http check timeout
		service.setCheck(check);

		register(service);
	}

	@Override
	protected void registerManagement() {
		NewService management = new NewService();
		management.setId(getManagementServiceId());
		management.setName(getManagementServiceName());
		management.setPort(getManagementPort());
		management.setTags(properties.getManagementTags());

		register(management);
	}

	protected void register(NewService newService) {
		log.info("Registering service with consul: {}", newService.toString());
		client.agentServiceRegister(newService);
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
		deregister(getContext().getId());
	}

	@Override
	protected void deregisterManagement() {
		deregister(getManagementServiceName());
	}

	private List<String> createTags() {
		List<String> tags = new LinkedList<>(properties.getTags());
		if(servletContext != null) {
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
}
