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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.util.InetUtils;

/**
 * Defines configuration for service discovery and registration.
 *
 * @author Spencer Gibb
 */
@ConfigurationProperties("spring.cloud.consul.discovery")
@Data
@CommonsLog
public class ConsulDiscoveryProperties {

	protected static final String MANAGEMENT = "management";

	@Getter(AccessLevel.PRIVATE)
	@Setter(AccessLevel.PRIVATE)
	private InetUtils.HostInfo hostInfo;

	private String aclToken;

	/** Tags to use when registering service */
	private List<String> tags = new ArrayList<>();

	/** Is service discovery enabled? */
	private boolean enabled = true;

	/** Tags to use when registering management service */
	private List<String> managementTags = Arrays.asList(MANAGEMENT);

	/** Alternate server path to invoke for health checking */
	private String healthCheckPath = "/health";

	/** Custom health check url to override default */
	private String healthCheckUrl;

	/** How often to perform the health check (e.g. 10s) */
	private String healthCheckInterval = "10s";

	/** Timeout for health check (e.g. 10s) */
	private String healthCheckTimeout;

	/** IP address to use when accessing service (must also set preferIpAddress
			to use) */
	private String ipAddress;

	/** Hostname to use when accessing server */
	private String hostname;

	/** Port to register the service under (defaults to listening port) */
	private Integer port;

	private Lifecycle lifecycle = new Lifecycle();

	/**
	 * Use ip address rather than hostname during registration
	 */
	private boolean preferIpAddress = false;

	private int catalogServicesWatchDelay = 10;

	private int catalogServicesWatchTimeout = 2;

	/** Unique service id */
	private String serviceId;

	/** Unique service instance id */
	private String instanceId;

	/** Whether to register an http or https service */
	private String scheme = "http";

	/** Suffix to use when registering management service */
	private String managementSuffix = MANAGEMENT;

	/**
	 * Map of serviceId's -> tag to query for in server list.
	 * This allows filtering services by a single tag.
	 */
	private Map<String, String> serverListQueryTags = new HashMap<>();

	private ConsulDiscoveryProperties() {}

	public ConsulDiscoveryProperties(InetUtils inetUtils) {
		this.hostInfo = inetUtils.findFirstNonLoopbackHostInfo();
		this.ipAddress = this.hostInfo.getIpAddress();
		this.hostname = this.hostInfo.getHostname();
	}

	public String getHostname() {
		return this.preferIpAddress ? this.ipAddress : this.hostname;
	}

	@Data
	public class Lifecycle {
		private boolean enabled = true;
	}
}
