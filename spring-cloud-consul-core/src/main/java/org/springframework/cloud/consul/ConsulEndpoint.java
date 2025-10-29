/*
 * Copyright 2013-present the original author or authors.
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.cloud.consul.model.http.agent.Service;
import org.springframework.cloud.consul.model.http.catalog.CatalogService;
import org.springframework.cloud.consul.model.http.catalog.Node;
import org.springframework.core.style.ToStringCreator;

/**
 * @author Spencer Gibb
 */
@Endpoint(id = "consul")
public class ConsulEndpoint {

	private final ConsulClient consul;

	public ConsulEndpoint(ConsulClient consul) {
		this.consul = consul;
	}

	@ReadOperation
	public ConsulData invoke() {
		ConsulData data = new ConsulData();
		// data.setKeyValues(kvClient.getKeyValueRecurse());
		Map<String, Service> agentServices = this.consul.getAgentServices().getBody();
		data.setAgentServices(agentServices);

		Map<String, List<String>> catalogServices = this.consul.getCatalogServices(null, null).getBody();

		for (String serviceId : catalogServices.keySet()) {
			List<CatalogService> response = this.consul.getCatalogService(serviceId).getBody();
			data.getCatalogServices().put(serviceId, response);
		}

		List<Node> catalogNodes = this.consul.getCatalogNodes().getBody();
		data.setCatalogNodes(catalogNodes);

		return data;
	}

	/**
	 * Represents Consul data related to catalog entries and agent servies.
	 */
	public static class ConsulData {

		Map<String, List<CatalogService>> catalogServices = new LinkedHashMap<>();

		Map<String, Service> agentServices;

		List<Node> catalogNodes;

		public ConsulData() {
		}

		public Map<String, List<CatalogService>> getCatalogServices() {
			return this.catalogServices;
		}

		public void setCatalogServices(Map<String, List<CatalogService>> catalogServices) {
			this.catalogServices = catalogServices;
		}

		public Map<String, Service> getAgentServices() {
			return this.agentServices;
		}

		public void setAgentServices(Map<String, Service> agentServices) {
			this.agentServices = agentServices;
		}

		public List<Node> getCatalogNodes() {
			return this.catalogNodes;
		}

		public void setCatalogNodes(List<Node> catalogNodes) {
			this.catalogNodes = catalogNodes;
		}

		@Override
		public String toString() {
			return new ToStringCreator(this).append("catalogServices", this.catalogServices)
				.append("agentServices", this.agentServices)
				.append("catalogNodes", this.catalogNodes)
				.toString();
		}

	}

}
