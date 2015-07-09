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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties("spring.cloud.consul.discovery")
@Data
@CommonsLog
public class ConsulDiscoveryProperties {

	@Getter(AccessLevel.PRIVATE)
	@Setter(AccessLevel.PRIVATE)
	private String[] hostInfo = initHostInfo();

	private List<String> tags = new ArrayList<>();

	private boolean enabled = true;

	private List<String> managementTags = Arrays.asList("management");

	private String healthCheckPath = "/health";

	private String healthCheckUrl;

	private String healthCheckInterval = "10s";

	private String ipAddress = this.hostInfo[0];

	private String hostname = hostInfo[1];

	private boolean preferIpAddress = false;

	private int catalogServicesWatchDelay = 10;

	private int catalogServicesWatchTimeout = 2;

	private String instanceId;

	private String scheme = "http";

	private boolean onlyPassingInstances;

	public String getHostname() {
		return this.preferIpAddress ? this.ipAddress : this.hostname;
	}

	private String[] initHostInfo() {
		String[] info = new String[2];
		try {
			info[0] = InetAddress.getLocalHost().getHostAddress();
			info[1] = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException ex) {
			log.error("Cannot get host info", ex);
		}
		return info;
	}
}
