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

package org.springframework.cloud.consul;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties("spring.cloud.consul")
@Validated
public class ConsulProperties {
	/** Consul agent hostname. Defaults to 'localhost'. */
	@NotNull
	private String host = "localhost";

	/** Consul agent scheme (HTTP/HTTPS). If there is no scheme in address - client will use HTTP. */
	private String scheme;

	/** Consul agent port. Defaults to '8500'. */
	@NotNull
	private int port = 8500;

	/** Is spring cloud consul enabled */
	private boolean enabled = true;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	@Override
	public String toString() {
		return "ConsulProperties{" +
				"host='" + host + '\'' +
				", port=" + port +
				", scheme=" + scheme +
				", enabled=" + enabled +
				'}';
	}
}
