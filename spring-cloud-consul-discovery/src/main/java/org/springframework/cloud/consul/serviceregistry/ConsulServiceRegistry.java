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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.discovery.TtlScheduler;
import org.springframework.util.ReflectionUtils;

import com.ecwid.consul.ConsulException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.Check;

import static org.springframework.boot.actuate.health.Status.OUT_OF_SERVICE;
import static org.springframework.boot.actuate.health.Status.UP;

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
		client.agentServiceDeregister(reg.getInstanceId(), properties.getAclToken());
	}

	@Override
	public void close() {

	}

	@Override
	public void setStatus(ConsulRegistration registration, String status) {
		if (status.equalsIgnoreCase(OUT_OF_SERVICE.getCode())) {
			client.agentServiceSetMaintenance(registration.getInstanceId(), true);
		} else if (status.equalsIgnoreCase(UP.getCode())) {
			client.agentServiceSetMaintenance(registration.getInstanceId(), false);
		} else {
			throw new IllegalArgumentException("Unknown status: "+status);
		}

	}

	@Override
	public Object getStatus(ConsulRegistration registration) {
		String serviceId = registration.getServiceId();
		Response<List<Check>> response = client.getHealthChecksForService(serviceId, QueryParams.DEFAULT);
		List<Check> checks = response.getValue();

		for (Check check : checks) {
			if (check.getServiceId().equals(registration.getInstanceId())) {
				if (check.getName().equalsIgnoreCase("Service Maintenance Mode")) {
					return OUT_OF_SERVICE.getCode();
				}
			}
		}

		return UP.getCode();
	}
}
