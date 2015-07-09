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

import java.util.*;

import com.ecwid.consul.v1.health.model.Check;
import com.ecwid.consul.v1.health.model.HealthService;
import com.netflix.loadbalancer.Server;
import lombok.extern.slf4j.Slf4j;

import static com.ecwid.consul.v1.health.model.Check.CheckStatus.WARNING;

/**
 * @author Spencer Gibb
 */
@Slf4j
public class ConsulServer extends Server {

	private final MetaInfo metaInfo;
	private final String address;
	private final String node;
	//order of r/w head: critical, warn, green
	private final PriorityQueue<Check> checks;

	public ConsulServer(final HealthService service, boolean preferAddress) {
		super((preferAddress)? service.getNode().getAddress() : service.getNode().getNode(),
				service.getService().getPort());
		address = service.getNode().getAddress();
		node = service.getNode().getNode();
		checks = new PriorityQueue<>(service.getChecks().size()+1,
				new Comparator<Check>() {
					@Override
					public int compare(Check o1, Check o2) {
						return - o1.getStatus().compareTo(o2.getStatus());
					}
				});
		checks.addAll(service.getChecks());
		metaInfo = new MetaInfo() {
			@Override
			public String getAppName() {
				return service.getService().getService();
			}

			@Override
			public String getServerGroup() {
				return null;
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
	}

	@Override
	public MetaInfo getMetaInfo() {
		return metaInfo;
	}

	public String getAddress() {
		return address;
	}

	public String getNode() {
		return node;
	}

	private PriorityQueue<Check> getChecks() {
		return checks;
	}

	public Check.CheckStatus getLowestCheckStatus() {
		Check instanceHealth = getChecks().peek();
		log.debug("Instance lowest-health {} for serviceInstanceId {}", instanceHealth, getMetaInfo()
				.getInstanceId());
		return instanceHealth == null ? WARNING : instanceHealth.getStatus();
	}
}
