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

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ecwid.consul.v1.ConsulClient;

/**
 * @author Spencer Gibb
 */
@Configuration
public class TestConsulLifecycleConfiguration {
	@Autowired(required = false)
	private ServerProperties serverProperties;

	@Autowired(required = false)
	private TtlScheduler ttlScheduler;

	@Autowired(required = false)
	private ServletContext servletContext;

	@Bean
	public ConsulLifecycle consulLifecycle(ConsulClient consulClient, ConsulDiscoveryProperties discoveryProperties,
										   HeartbeatProperties heartbeatProperties) {
		ConsulLifecycle lifecycle = new ConsulLifecycle(consulClient, discoveryProperties, heartbeatProperties);
		if (this.ttlScheduler != null) {
			lifecycle.setTtlScheduler(this.ttlScheduler);
		}
		if (this.servletContext != null) {
			lifecycle.setServletContext(this.servletContext);
		}
		if (this.serverProperties != null && this.serverProperties.getPort() != null && this.serverProperties.getPort() > 0) {
			// no need to wait for events for this to start since the user has explicitly set the port.
			lifecycle.setPort(this.serverProperties.getPort());
		}
		return lifecycle;
	}
}
