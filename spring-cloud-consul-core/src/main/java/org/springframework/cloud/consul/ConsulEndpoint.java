/*
 * Copyright 2013-2015 the original author or authors.
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

import lombok.Data;

import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.ecwid.consul.v1.catalog.model.Node;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties(prefix = "endpoints.consul", ignoreUnknownFields = false)
public class ConsulEndpoint extends AbstractEndpoint<ConsulEndpoint.ConsulData> {

	private ConsulClient consul;

	public ConsulEndpoint(ConsulClient consul) {
		super("consul", false, true);
		this.consul = consul;
	}

	@Override
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

	@Data
	public static class ConsulData {
		Map<String, List<CatalogService>> catalogServices = new LinkedHashMap<>();

		Map<String, Service> agentServices;

		List<Node> catalogNodes;

		// List<KeyValue> keyValues;
	}
}
