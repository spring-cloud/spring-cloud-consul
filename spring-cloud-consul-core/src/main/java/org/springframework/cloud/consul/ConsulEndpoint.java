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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.ecwid.consul.v1.catalog.model.Node;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.core.style.ToStringCreator;

/**
 * @author Spencer Gibb
 */
@Endpoint(id = "consul")
public class ConsulEndpoint {

	private ConsulClient consul;

	public ConsulEndpoint(ConsulClient consul) {
		this.consul = consul;
	}

	@ReadOperation
	public ConsulData invoke() {
		ConsulData data = new ConsulData();
		// data.setKeyValues(kvClient.getKeyValueRecurse());
		Response<Map<String, Service>> agentServices = consul.getAgentServices();
		data.setAgentServices(agentServices.getValue());

		Response<Map<String, List<String>>> catalogServices = consul
				.getCatalogServices(QueryParams.DEFAULT);

		for (String serviceId : catalogServices.getValue().keySet()) {
			Response<List<CatalogService>> response = consul.getCatalogService(serviceId,
					QueryParams.DEFAULT);
			data.getCatalogServices().put(serviceId, response.getValue());
		}

		Response<List<Node>> catalogNodes = consul.getCatalogNodes(QueryParams.DEFAULT);
		data.setCatalogNodes(catalogNodes.getValue());

		return data;
	}

	public static class ConsulData {
		Map<String, List<CatalogService>> catalogServices = new LinkedHashMap<>();

		Map<String, Service> agentServices;

		List<Node> catalogNodes;

		public ConsulData() {
		}

		public Map<String, List<CatalogService>> getCatalogServices() {
			return this.catalogServices;
		}

		public Map<String, Service> getAgentServices() {
			return this.agentServices;
		}

		public List<Node> getCatalogNodes() {
			return this.catalogNodes;
		}

		public void setCatalogServices(Map<String, List<CatalogService>> catalogServices) {
			this.catalogServices = catalogServices;
		}

		public void setAgentServices(Map<String, Service> agentServices) {
			this.agentServices = agentServices;
		}

		public void setCatalogNodes(List<Node> catalogNodes) {
			this.catalogNodes = catalogNodes;
		}

		@Override
		public String toString() {
			return new ToStringCreator(this)
					.append("catalogServices", catalogServices)
					.append("agentServices", agentServices)
					.append("catalogNodes", catalogNodes)
					.toString();
		}
	}
}
