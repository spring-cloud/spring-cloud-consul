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

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.AbstractDiscoveryLifecycle;
import org.springframework.cloud.consul.ConsulProperties;

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
	private ConsulProperties consulProperties;

	@Autowired
	private TtlScheduler ttlScheduler;

	@Autowired
	private HeartbeatProperties ttlConfig;

    @Autowired(required = false)
    private ServletContext servletContext;

    @Override
	protected void register() {
		NewService service = new NewService();
		String appName = getAppName();
		// TODO: move id to properties with context ID as default
		service.setId(getContext().getId());
		service.setName(appName);
		// TODO: support port = 0 random assignment
		Integer port = new Integer(getEnvironment().getProperty("server.port", "8080"));
		service.setPort(port);
		service.setTags(createTags());
		NewService.Check check = new NewService.Check();
		check.setTtl(ttlConfig.getTtl());
		service.setCheck(check);
		register(service);
	}

    private List<String> createTags() {
        List<String> tags = new LinkedList<>(consulProperties.getTags());
        if(servletContext != null) {
            tags.add("contextPath=" + servletContext.getContextPath());
        }
        return tags;
    }

    @Override
	protected void registerManagement() {
		NewService management = new NewService();
		management.setId(getManagementServiceId());
		management.setName(getManagementServiceName());
		management.setPort(getManagementPort());
		management.setTags(consulProperties.getManagementTags());

		register(management);
	}

	protected void register(NewService service) {
		log.info("Registering service with consul: {}", service.toString());
		client.agentServiceRegister(service);
		ttlScheduler.add(service);
	}

	@Override
	protected Object getConfiguration() {
		return consulProperties;
	}

	@Override
	protected void deregister() {
		deregister(getContext().getId());
	}

	@Override
	protected void deregisterManagement() {
		deregister(getManagementServiceName());
	}

	private void deregister(String serviceId) {
		ttlScheduler.remove(serviceId);
		client.agentServiceDeregister(serviceId);
	}

	@Override
	protected boolean isEnabled() {
		return consulProperties.isEnabled();
	}
}
