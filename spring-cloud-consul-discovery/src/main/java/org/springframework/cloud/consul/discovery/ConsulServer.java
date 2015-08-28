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

import static org.springframework.cloud.consul.discovery.IpAddressUtils.getCatalogServiceHost;

import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.netflix.loadbalancer.Server;

/**
 * @author Spencer Gibb
 */
public class ConsulServer extends Server {

	private final MetaInfo metaInfo;
	private final String address;
	private final String node;

	public ConsulServer(final CatalogService service, boolean preferAddress) {
		super(getCatalogServiceHost(service, preferAddress), service.getServicePort());
		address = service.getAddress();
		node = service.getNode();
		metaInfo = new MetaInfo() {
			@Override
			public String getAppName() {
				return service.getServiceName();
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
				return service.getServiceId();
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
}
