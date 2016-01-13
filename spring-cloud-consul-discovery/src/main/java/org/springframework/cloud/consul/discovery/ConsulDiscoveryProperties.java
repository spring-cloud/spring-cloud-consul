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
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.util.InetUtils;

/**
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

	private List<String> tags = new ArrayList<>();

	private boolean enabled = true;

	private List<String> managementTags = Arrays.asList(MANAGEMENT);

	private String healthCheckPath = "/health";

	private String healthCheckUrl;

	private String healthCheckInterval = "10s";

	private String healthCheckTimeout;

	private String ipAddress;

	private String hostname;

	private Integer externalPort;

	private Lifecycle lifecycle = new Lifecycle();

	/**
	 * Use ip address rather than hostname during registration
	 */
	private boolean preferIpAddress = false;

	private int catalogServicesWatchDelay = 10;

	private int catalogServicesWatchTimeout = 2;

	private String instanceId;

	private String scheme = "http";

	private String managementSuffix = MANAGEMENT;

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
