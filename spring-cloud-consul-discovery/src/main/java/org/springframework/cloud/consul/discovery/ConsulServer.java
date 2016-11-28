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

import java.util.Map;

import com.ecwid.consul.v1.health.model.Check;
import com.ecwid.consul.v1.health.model.HealthService;
import com.netflix.loadbalancer.Server;

import static org.springframework.cloud.consul.discovery.ConsulServerUtils.findHost;

/**
 * @author Spencer Gibb
 */
public class ConsulServer extends Server {

	private final MetaInfo metaInfo;
	private final HealthService service;
	private final Map<String, String> metadata;

	public ConsulServer(final HealthService healthService) {
		super(findHost(healthService), healthService.getService().getPort());
		this.service = healthService;
		this.metadata = ConsulServerUtils.getMetadata(this.service);
		metaInfo = new MetaInfo() {
			@Override
			public String getAppName() {
				return service.getService().getService();
			}

			@Override
			public String getServerGroup() {
				return getMetadata().get("group");
			}

			@Override
			public String getServiceIdForDiscovery() {
				return null;
			}

			@Override
			public String getInstanceId() {
				return service.getService().getId();
			}
		};

		setAlive(isPassingChecks());
	}

	@Override
	public MetaInfo getMetaInfo() {
		return metaInfo;
	}

	public HealthService getHealthService() {
		return this.service;
	}

	public Map<String, String> getMetadata() {
		return metadata;
	}

	public boolean isPassingChecks() {
		for (Check check : this.service.getChecks()) {
			if (check.getStatus() != Check.CheckStatus.PASSING) {
				return false;
			}
		}
		return true;
	}
}
