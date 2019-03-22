/*
 * Copyright 2013-2019 the original author or authors.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtils.HostInfo;
import org.springframework.core.style.ToStringCreator;

/**
 * Defines configuration for service discovery and registration.
 *
 * @author Spencer Gibb
 * @author Donnabell Dmello
 * @author Venil Noronha
 * @author Richard Kettelerij
 */
@ConfigurationProperties("spring.cloud.consul.discovery")
public class ConsulDiscoveryProperties {

	protected static final String MANAGEMENT = "management";

	private HostInfo hostInfo;

	@Value("${consul.token:${CONSUL_TOKEN:${spring.cloud.consul.token:${SPRING_CLOUD_CONSUL_TOKEN:}}}}")
	private String aclToken;

	/** Tags to use when registering service. */
	private List<String> tags = new ArrayList<>();

	/** Is service discovery enabled? */
	private boolean enabled = true;

	/** Tags to use when registering management service. */
	private List<String> managementTags = new ArrayList<>();

	/** Alternate server path to invoke for health checking. */
	private String healthCheckPath = "/actuator/health";

	/** Custom health check url to override default. */
	private String healthCheckUrl;

	/** Headers to be applied to the Health Check calls. */
	private Map<String, List<String>> healthCheckHeaders = new HashMap<>();

	/** How often to perform the health check (e.g. 10s), defaults to 10s. */
	private String healthCheckInterval = "10s";

	/** Timeout for health check (e.g. 10s). */
	private String healthCheckTimeout;

	/**
	 * Timeout to deregister services critical for longer than timeout (e.g. 30m).
	 * Requires consul version 7.x or higher.
	 */
	private String healthCheckCriticalTimeout;

	/**
	 * IP address to use when accessing service (must also set preferIpAddress to use).
	 */
	private String ipAddress;

	/** Hostname to use when accessing server. */
	private String hostname;

	/** Port to register the service under (defaults to listening port). */
	private Integer port;

	/** Port to register the management service under (defaults to management port). */
	private Integer managementPort;

	private Lifecycle lifecycle = new Lifecycle();

	/** Use ip address rather than hostname during registration. */
	private boolean preferIpAddress = false;

	/** Source of how we will determine the address to use. */
	private boolean preferAgentAddress = false;

	/** The delay between calls to watch consul catalog in millis, default is 1000. */
	private int catalogServicesWatchDelay = 1000;

	/** The number of seconds to block while watching consul catalog, default is 2. */
	private int catalogServicesWatchTimeout = 2;

	/** Service name. */
	private String serviceName;

	/** Unique service instance id. */
	private String instanceId;

	/** Service instance zone. */
	private String instanceZone;

	/** Service instance group. */
	private String instanceGroup;

	/**
	 * Service instance zone comes from metadata. This allows changing the metadata tag
	 * name.
	 */
	private String defaultZoneMetadataName = "zone";

	/** Whether to register an http or https service. */
	private String scheme = "http";

	/** Suffix to use when registering management service. */
	private String managementSuffix = MANAGEMENT;

	/**
	 * Map of serviceId's -> tag to query for in server list. This allows filtering
	 * services by a single tag.
	 */
	private Map<String, String> serverListQueryTags = new HashMap<>();

	/**
	 * Map of serviceId's -> datacenter to query for in server list. This allows looking
	 * up services in another datacenters.
	 */
	private Map<String, String> datacenters = new HashMap<>();

	/** Tag to query for in service list if one is not listed in serverListQueryTags. */
	private String defaultQueryTag;

	/**
	 * Add the 'passing` parameter to /v1/health/service/serviceName. This pushes health
	 * check passing to the server.
	 */
	private boolean queryPassing = false;

	/** Register as a service in consul. */
	private boolean register = true;

	/** Disable automatic de-registration of service in consul. */
	private boolean deregister = true;

	/** Register health check in consul. Useful during development of a service. */
	private boolean registerHealthCheck = true;

	/**
	 * Throw exceptions during service registration if true, otherwise, log warnings
	 * (defaults to true).
	 */
	private boolean failFast = true;

	/**
	 * Skips certificate verification during service checks if true, otherwise runs
	 * certificate verification.
	 */
	private Boolean healthCheckTlsSkipVerify;

	/**
	 * Order of the discovery client used by `CompositeDiscoveryClient` for sorting
	 * available clients.
	 */
	private int order = 0;

	@SuppressWarnings("unused")
	private ConsulDiscoveryProperties() {
		this.managementTags.add(MANAGEMENT);
	}

	public ConsulDiscoveryProperties(InetUtils inetUtils) {
		this();
		this.hostInfo = inetUtils.findFirstNonLoopbackHostInfo();
		this.ipAddress = this.hostInfo.getIpAddress();
		this.hostname = this.hostInfo.getHostname();
	}

	/**
	 * @param serviceId The service who's filtering tag is being looked up
	 * @return The tag the given service id should be filtered by, or null.
	 */
	public String getQueryTagForService(String serviceId) {
		String tag = this.serverListQueryTags.get(serviceId);
		return tag != null ? tag : this.defaultQueryTag;
	}

	public String getHostname() {
		return this.preferIpAddress ? this.ipAddress : this.hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
		this.hostInfo.override = true;
	}

	private HostInfo getHostInfo() {
		return this.hostInfo;
	}

	private void setHostInfo(HostInfo hostInfo) {
		this.hostInfo = hostInfo;
	}

	public String getAclToken() {
		return this.aclToken;
	}

	public void setAclToken(String aclToken) {
		this.aclToken = aclToken;
	}

	public List<String> getTags() {
		return this.tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public List<String> getManagementTags() {
		return this.managementTags;
	}

	public void setManagementTags(List<String> managementTags) {
		this.managementTags = managementTags;
	}

	public String getHealthCheckPath() {
		return this.healthCheckPath;
	}

	public void setHealthCheckPath(String healthCheckPath) {
		this.healthCheckPath = healthCheckPath;
	}

	public String getHealthCheckUrl() {
		return this.healthCheckUrl;
	}

	public void setHealthCheckUrl(String healthCheckUrl) {
		this.healthCheckUrl = healthCheckUrl;
	}

	public Map<String, List<String>> getHealthCheckHeaders() {
		return this.healthCheckHeaders;
	}

	public void setHealthCheckHeaders(Map<String, List<String>> healthCheckHeaders) {
		this.healthCheckHeaders = healthCheckHeaders;
	}

	public String getHealthCheckInterval() {
		return this.healthCheckInterval;
	}

	public void setHealthCheckInterval(String healthCheckInterval) {
		this.healthCheckInterval = healthCheckInterval;
	}

	public String getHealthCheckTimeout() {
		return this.healthCheckTimeout;
	}

	public void setHealthCheckTimeout(String healthCheckTimeout) {
		this.healthCheckTimeout = healthCheckTimeout;
	}

	public String getHealthCheckCriticalTimeout() {
		return this.healthCheckCriticalTimeout;
	}

	public void setHealthCheckCriticalTimeout(String healthCheckCriticalTimeout) {
		this.healthCheckCriticalTimeout = healthCheckCriticalTimeout;
	}

	public String getIpAddress() {
		return this.ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
		this.hostInfo.override = true;
	}

	public Integer getPort() {
		return this.port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Integer getManagementPort() {
		return this.managementPort;
	}

	public void setManagementPort(Integer managementPort) {
		this.managementPort = managementPort;
	}

	public Lifecycle getLifecycle() {
		return this.lifecycle;
	}

	public void setLifecycle(Lifecycle lifecycle) {
		this.lifecycle = lifecycle;
	}

	public boolean isPreferIpAddress() {
		return this.preferIpAddress;
	}

	public void setPreferIpAddress(boolean preferIpAddress) {
		this.preferIpAddress = preferIpAddress;
	}

	public boolean isPreferAgentAddress() {
		return this.preferAgentAddress;
	}

	public void setPreferAgentAddress(boolean preferAgentAddress) {
		this.preferAgentAddress = preferAgentAddress;
	}

	public int getCatalogServicesWatchDelay() {
		return this.catalogServicesWatchDelay;
	}

	public void setCatalogServicesWatchDelay(int catalogServicesWatchDelay) {
		this.catalogServicesWatchDelay = catalogServicesWatchDelay;
	}

	public int getCatalogServicesWatchTimeout() {
		return this.catalogServicesWatchTimeout;
	}

	public void setCatalogServicesWatchTimeout(int catalogServicesWatchTimeout) {
		this.catalogServicesWatchTimeout = catalogServicesWatchTimeout;
	}

	public String getServiceName() {
		return this.serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getInstanceId() {
		return this.instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getInstanceZone() {
		return this.instanceZone;
	}

	public void setInstanceZone(String instanceZone) {
		this.instanceZone = instanceZone;
	}

	public String getInstanceGroup() {
		return this.instanceGroup;
	}

	public void setInstanceGroup(String instanceGroup) {
		this.instanceGroup = instanceGroup;
	}

	public String getDefaultZoneMetadataName() {
		return this.defaultZoneMetadataName;
	}

	public void setDefaultZoneMetadataName(String defaultZoneMetadataName) {
		this.defaultZoneMetadataName = defaultZoneMetadataName;
	}

	public String getScheme() {
		return this.scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String getManagementSuffix() {
		return this.managementSuffix;
	}

	public void setManagementSuffix(String managementSuffix) {
		this.managementSuffix = managementSuffix;
	}

	public Map<String, String> getServerListQueryTags() {
		return this.serverListQueryTags;
	}

	public void setServerListQueryTags(Map<String, String> serverListQueryTags) {
		this.serverListQueryTags = serverListQueryTags;
	}

	public Map<String, String> getDatacenters() {
		return this.datacenters;
	}

	public void setDatacenters(Map<String, String> datacenters) {
		this.datacenters = datacenters;
	}

	public String getDefaultQueryTag() {
		return this.defaultQueryTag;
	}

	public void setDefaultQueryTag(String defaultQueryTag) {
		this.defaultQueryTag = defaultQueryTag;
	}

	public boolean isQueryPassing() {
		return this.queryPassing;
	}

	public void setQueryPassing(boolean queryPassing) {
		this.queryPassing = queryPassing;
	}

	public boolean isRegister() {
		return this.register;
	}

	public void setRegister(boolean register) {
		this.register = register;
	}

	public boolean isDeregister() {
		return this.deregister;
	}

	public void setDeregister(boolean deregister) {
		this.deregister = deregister;
	}

	public boolean isRegisterHealthCheck() {
		return this.registerHealthCheck;
	}

	public void setRegisterHealthCheck(boolean registerHealthCheck) {
		this.registerHealthCheck = registerHealthCheck;
	}

	public boolean isFailFast() {
		return this.failFast;
	}

	public void setFailFast(boolean failFast) {
		this.failFast = failFast;
	}

	public Boolean getHealthCheckTlsSkipVerify() {
		return this.healthCheckTlsSkipVerify;
	}

	public void setHealthCheckTlsSkipVerify(Boolean healthCheckTlsSkipVerify) {
		this.healthCheckTlsSkipVerify = healthCheckTlsSkipVerify;
	}

	public int getOrder() {
		return this.order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("hostInfo", this.hostInfo)
				.append("aclToken", this.aclToken).append("tags", this.tags)
				.append("enabled", this.enabled)
				.append("managementTags", this.managementTags)
				.append("healthCheckPath", this.healthCheckPath)
				.append("healthCheckUrl", this.healthCheckUrl)
				.append("healthCheckHeaders", this.healthCheckHeaders)
				.append("healthCheckInterval", this.healthCheckInterval)
				.append("healthCheckTimeout", this.healthCheckTimeout)
				.append("healthCheckCriticalTimeout", this.healthCheckCriticalTimeout)
				.append("ipAddress", this.ipAddress).append("hostname", this.hostname)
				.append("port", this.port).append("managementPort", this.managementPort)
				.append("lifecycle", this.lifecycle)
				.append("preferIpAddress", this.preferIpAddress)
				.append("preferAgentAddress", this.preferAgentAddress)
				.append("catalogServicesWatchDelay", this.catalogServicesWatchDelay)
				.append("catalogServicesWatchTimeout", this.catalogServicesWatchTimeout)
				.append("serviceName", this.serviceName)
				.append("instanceId", this.instanceId)
				.append("instanceZone", this.instanceZone)
				.append("instanceGroup", this.instanceGroup)
				.append("defaultZoneMetadataName", this.defaultZoneMetadataName)
				.append("scheme", this.scheme)
				.append("managementSuffix", this.managementSuffix)
				.append("serverListQueryTags", this.serverListQueryTags)
				.append("datacenters", this.datacenters)
				.append("defaultQueryTag", this.defaultQueryTag)
				.append("queryPassing", this.queryPassing)
				.append("register", this.register).append("deregister", this.deregister)
				.append("registerHealthCheck", this.registerHealthCheck)
				.append("failFast", this.failFast)
				.append("healthCheckTlsSkipVerify", this.healthCheckTlsSkipVerify)
				.append("order", this.order).toString();
	}

	/**
	 * Properties releated to the lifecycle.
	 */
	public static class Lifecycle {

		private boolean enabled = true;

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		@Override
		public String toString() {
			return "Lifecycle{" + "enabled=" + this.enabled + '}';
		}

	}

}
