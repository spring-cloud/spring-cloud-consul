/*
 * Copyright 2013-2024 the original author or authors.
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

package org.springframework.cloud.consul;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.consul.model.http.agent.Service;
import org.springframework.cloud.consul.model.http.catalog.CatalogService;
import org.springframework.cloud.consul.model.http.catalog.Node;
import org.springframework.web.service.annotation.GetExchange;

public interface IConsulClient {

	@GetExchange("/v1/status/leader")
	String getStatusLeader();

	@GetExchange("/v1/status/peers")
	List<String> getStatusPeers();

	@GetExchange("/v1/catalog/services")
	Map<String, List<String>> getCatalogServices();

	@GetExchange("/v1/agent/services")
	Map<String, Service> getAgentServices();

	@GetExchange("/v1/catalog/services")
	List<CatalogService> getCatalogService(String serviceId);

	@GetExchange("/v1/catalog/nodes")
	List<Node> getCatalogNodes();

}
