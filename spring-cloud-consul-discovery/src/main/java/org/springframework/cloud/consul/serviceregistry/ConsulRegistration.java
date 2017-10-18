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

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

import com.ecwid.consul.v1.agent.model.NewService;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.ConsulServerUtils;

import java.net.URI;
import java.util.Map;

/**
 * @author Spencer Gibb
 */
public class ConsulRegistration implements Registration {

	private final NewService service;
	private ConsulDiscoveryProperties properties;

	public ConsulRegistration(NewService service, ConsulDiscoveryProperties properties) {
		this.service = service;
		this.properties = properties;
	}

	public NewService getService() {
		return service;
	}

	protected ConsulDiscoveryProperties getProperties() {
		return properties;
	}

	public String getInstanceId() {
		return getService().getId();
	}

	public String getServiceId() {
		return getService().getName();
	}

	@Override
	public String getHost() {
		return getService().getAddress();
	}

	@Override
	public int getPort() {
		return getService().getPort();
	}

	@Override
	public boolean isSecure() {
		return this.properties.getScheme().equalsIgnoreCase("https");
	}

	@Override
	public URI getUri() {
		return DefaultServiceInstance.getUri(this);
	}

	@Override
	public Map<String, String> getMetadata() {
		return ConsulServerUtils.getMetadata(getService().getTags());
	}
}
