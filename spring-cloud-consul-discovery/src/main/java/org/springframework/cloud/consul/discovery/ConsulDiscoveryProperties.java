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

package org.springframework.cloud.consul.discovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Defines configuration for service discovery and registration.
 *
 * @author Spencer Gibb
 * @author Venil Noronha
 */
@ConfigurationProperties("spring.cloud.consul.discovery")
@Data
public class ConsulDiscoveryProperties {

	protected static final String MANAGEMENT = "management";

	@Getter(AccessLevel.PRIVATE)
	@Setter(AccessLevel.PRIVATE)
	private InetUtils.HostInfo hostInfo;

	@Value("${consul.token:${CONSUL_TOKEN:${spring.cloud.consul.token:${SPRING_CLOUD_CONSUL_TOKEN:}}}}")
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

	/** Port to register the management service under (defaults to management port) */
	private Integer managementPort;

	private Lifecycle lifecycle = new Lifecycle();

	/**
	 * Use ip address rather than hostname during registration
	 */
	private boolean preferIpAddress = false;
	
	/**
	 * Source of how we will determine the address to use
	 */
	private boolean preferAgentAddress = false;
	
	private int catalogServicesWatchDelay = 10;

	private int catalogServicesWatchTimeout = 2;

	/** Service name */
	private String serviceName;

	/** Unique service instance id */
	private String instanceId;

	/** Service instance zone */
	private String instanceZone;

	/** Service instance group*/
	private String instanceGroup;

	/**
	 * Service instance zone comes from metadata.
	 * This allows changing the metadata tag name.
	 */
	private String defaultZoneMetadataName = "zone";

	/** Whether to register an http or https service */
	private String scheme = "http";

	/** Suffix to use when registering management service */
	private String managementSuffix = MANAGEMENT;

	/**
	 * Map of serviceId's -> tag to query for in server list.
	 * This allows filtering services by a single tag.
	 */
	private Map<String, String> serverListQueryTags = new HashMap<>();

	/**
	 * Tag to query for in service list if one is not listed in serverListQueryTags.
	 */
	private String defaultQueryTag;

	/**
	 * Add the 'passing` parameter to /v1/health/service/serviceName.
	 * This pushes health check passing to the server.
	 */
	private boolean queryPassing = false;

	/**
	 * Register as a service in consul.
	 */
	private boolean register = true;

	/**
	 * Register health check in consul. Useful during development of a service.
	 */
	private boolean registerHealthCheck = true;

	/**
	 * Throw exceptions during service registration if true, otherwise, log
	 * warnings (defaults to true).
	 */
	private boolean failFast = true;

	@SuppressWarnings("unused")
	private ConsulDiscoveryProperties() {}

	public ConsulDiscoveryProperties(InetUtils inetUtils) {
		this.hostInfo = inetUtils.findFirstNonLoopbackHostInfo();
		this.ipAddress = this.hostInfo.getIpAddress();
		this.hostname = this.hostInfo.getHostname();
	}

	/**
	 *
	 * @param serviceId The service who's filtering tag is being looked up
	 * @return The tag the given service id should be filtered by, or null.
     */
	public String getQueryTagForService(String serviceId){
		String tag = serverListQueryTags.get(serviceId);
		return tag != null ? tag : defaultQueryTag;
	}

	public String getHostname() {
		return this.preferIpAddress ? this.ipAddress : this.hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
		this.hostInfo.override = true;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
		this.hostInfo.override = true;
	}

	@Data
	public static class Lifecycle {
		private boolean enabled = true;
	}
}
