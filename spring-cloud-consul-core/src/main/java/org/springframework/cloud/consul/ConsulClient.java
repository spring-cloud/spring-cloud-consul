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

import java.util.List;
import java.util.Map;

import org.springframework.cloud.consul.model.http.agent.Service;
import org.springframework.cloud.consul.model.http.catalog.CatalogService;
import org.springframework.cloud.consul.model.http.catalog.Node;
import org.springframework.cloud.consul.model.http.event.Event;
import org.springframework.cloud.consul.model.http.format.WaitTimeFormat;
import org.springframework.cloud.consul.model.http.kv.GetValue;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;

public interface ConsulClient {

	/**
	 * Header name for Consul ACL Tokens.
	 */
	String ACL_TOKEN_HEADER = "X-Consul-Token";

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

	@GetExchange("/v1/kv/{context}")
	ResponseEntity<List<GetValue>> getKVValue(@PathVariable String context,
			@RequestHeader(value = ACL_TOKEN_HEADER, required = false) String aclToken);

	@GetExchange("/v1/kv/{context}?recurse")
	ResponseEntity<List<GetValue>> getKVValues(@PathVariable String context,
			@RequestHeader(value = ACL_TOKEN_HEADER, required = false) String aclToken);

	@GetExchange("/v1/kv/{context}?recurse")
	ResponseEntity<List<GetValue>> getKVValues(@PathVariable String context,
			@RequestHeader(value = ACL_TOKEN_HEADER, required = false) String aclToken,
			@RequestParam("wait") @WaitTimeFormat Long waitTime, @RequestParam("index") long index);

	@GetExchange("/v1/events")
	ResponseEntity<List<Event>> eventList();

	@GetExchange("/v1/events")
	ResponseEntity<List<Event>> eventList(int eventTimeout, long index);

	@PostExchange("/v1/events")
	ResponseEntity<Event> eventFire(String name, @RequestBody String payload);

}
