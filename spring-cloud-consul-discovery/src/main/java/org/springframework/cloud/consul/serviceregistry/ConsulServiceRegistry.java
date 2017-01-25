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

import com.ecwid.consul.ConsulException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.HealthService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.discovery.TtlScheduler;
import org.springframework.util.ReflectionUtils;

import java.util.List;

/**
 * @author Spencer Gibb
 */
public class ConsulServiceRegistry implements ServiceRegistry<ConsulRegistration> {

	private static Log log = LogFactory.getLog(ConsulServiceRegistry.class);

	private final ConsulClient client;

	private final ConsulDiscoveryProperties properties;

	private final TtlScheduler ttlScheduler;

	private final HeartbeatProperties heartbeatProperties;

	public ConsulServiceRegistry(ConsulClient client, ConsulDiscoveryProperties properties, TtlScheduler ttlScheduler, HeartbeatProperties heartbeatProperties) {
		this.client = client;
		this.properties = properties;
		this.ttlScheduler = ttlScheduler;
		this.heartbeatProperties = heartbeatProperties;
	}

	@Override
	public void register(ConsulRegistration reg) {
		log.info("Registering service with consul: " + reg.getService());
		try {
			client.agentServiceRegister(reg.getService(), properties.getAclToken());
			if (heartbeatProperties.isEnabled() && ttlScheduler != null) {
				ttlScheduler.add(reg.getInstanceId());
			}
		}
		catch (ConsulException e) {
			if (this.properties.isFailFast()) {
				log.error("Error registering service with consul: " + reg.getService(), e);
				ReflectionUtils.rethrowRuntimeException(e);
			}
			log.warn("Failfast is false. Error registering service with consul: " + reg.getService(), e);
		}
	}

	@Override
	public void deregister(ConsulRegistration reg) {
		if (ttlScheduler != null) {
			ttlScheduler.remove(reg.getInstanceId());
		}
		if (log.isInfoEnabled()) {
			log.info("Deregistering service with consul: " + reg.getInstanceId());
		}
		client.agentServiceDeregister(reg.getInstanceId());
	}

	@Override
	public void close() {

	}

	@Override
	public void setStatus(ConsulRegistration registration, String status) {
		if (status.equalsIgnoreCase("out_of_service")) {
			client.agentServiceSetMaintenance(registration.getInstanceId(), true);
		} else if (status.equalsIgnoreCase("up")) {
			client.agentServiceSetMaintenance(registration.getInstanceId(), false);
		} else {
			throw new IllegalArgumentException("Unknown status: "+status);
		}

	}

	@Override
	public Object getStatus(ConsulRegistration registration) {
		final String serviceId = registration.getServiceId();
		Response<List<HealthService>> healthServices = client.getHealthServices(serviceId,
				this.properties.getQueryTagForService(serviceId), false,
				QueryParams.DEFAULT, this.properties.getAclToken());
		return healthServices.getValue();
	}
}
