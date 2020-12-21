/*
 * Copyright 2015-2020 the original author or authors.
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

package org.springframework.cloud.consul.discovery;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ecwid.consul.v1.health.model.HealthService;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.core.style.ToStringCreator;

import static org.springframework.cloud.consul.discovery.ConsulServerUtils.findHost;

public class ConsulServiceInstance extends DefaultServiceInstance {

	private HealthService healthService;

	public ConsulServiceInstance(HealthService healthService, String serviceId) {
		this(healthService.getService().getId(), serviceId, findHost(healthService),
				healthService.getService().getPort(), getSecure(healthService), getMetadata(healthService),
				healthService.getService().getTags());
		this.healthService = healthService;
	}

	public ConsulServiceInstance(String instanceId, String serviceId, String host, int port, boolean secure,
			Map<String, String> metadata, List<String> tags) {
		super(instanceId, serviceId, host, port, secure, metadata);
	}

	public ConsulServiceInstance(String instanceId, String serviceId, String host, int port, boolean secure) {
		super(instanceId, serviceId, host, port, secure);
	}

	public ConsulServiceInstance() {
	}

	private static Map<String, String> getMetadata(HealthService healthService) {
		Map<String, String> metadata = healthService.getService().getMeta();
		if (metadata == null) {
			metadata = new LinkedHashMap<>();
		}
		return metadata;
	}

	private static boolean getSecure(HealthService healthService) {
		boolean secure = false;
		Map<String, String> metadata = getMetadata(healthService);
		if (metadata.containsKey("secure")) {
			secure = Boolean.parseBoolean(metadata.get("secure"));
		}
		return secure;
	}

	public HealthService getHealthService() {
		return this.healthService;
	}

	public void setHealthService(HealthService healthService) {
		this.healthService = healthService;
	}

	public List<String> getTags() {
		if (healthService != null) {
			return healthService.getService().getTags();
		}
		return Collections.emptyList();
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("instanceId", getInstanceId()).append("serviceId", getServiceId())
				.append("host", getHost()).append("port", getPort()).append("secure", isSecure())
				.append("metadata", getMetadata()).append("uri", getUri()).append("healthService", healthService)
				.toString();

	}

}
