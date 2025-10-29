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

import org.springframework.cloud.consul.model.http.agent.NewService;
import org.springframework.cloud.consul.model.http.agent.Service;
import org.springframework.cloud.consul.model.http.catalog.CatalogService;
import org.springframework.cloud.consul.model.http.catalog.Node;
import org.springframework.cloud.consul.model.http.event.Event;
import org.springframework.cloud.consul.model.http.format.WaitTimeFormat;
import org.springframework.cloud.consul.model.http.health.Check;
import org.springframework.cloud.consul.model.http.health.HealthService;
import org.springframework.cloud.consul.model.http.kv.GetValue;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

public interface ConsulClient {

	/**
	 * Header name for Consul ACL Tokens.
	 */
	String ACL_TOKEN_HEADER = "X-Consul-Token";

	@GetExchange("/v1/status/leader")
	ResponseEntity<String> getStatusLeader();

	@GetExchange("/v1/status/peers")
	ResponseEntity<List<String>> getStatusPeers();

	@GetExchange("/v1/catalog/datacenters")
	ResponseEntity<List<String>> getCatalogDatacenters(
			@RequestHeader(name = ACL_TOKEN_HEADER, required = false) String aclToken);

	@GetExchange("/v1/catalog/services")
	ResponseEntity<Map<String, List<String>>> getCatalogServices();

	@GetExchange("/v1/catalog/services")
	ResponseEntity<Map<String, List<String>>> getCatalogServices(
			@RequestHeader(name = ACL_TOKEN_HEADER, required = false) String aclToken, QueryParams queryParams);

	@PutExchange("/v1/agent/check/fail/{checkId}")
	ResponseEntity<Void> agentCheckFail(String checkId, @RequestParam(required = false) String note,
			@RequestHeader(name = ACL_TOKEN_HEADER, required = false) String aclToken);

	@PutExchange("/v1/agent/check/pass/{checkId}")
	ResponseEntity<Void> agentCheckPass(String checkId, @RequestParam(required = false) String note,
			@RequestHeader(name = ACL_TOKEN_HEADER, required = false) String aclToken);

	@PutExchange("/v1/agent/check/warn/{checkId}")
	ResponseEntity<Void> agentCheckWarn(String checkId, @RequestParam(required = false) String note,
			@RequestHeader(name = ACL_TOKEN_HEADER, required = false) String aclToken);

	@GetExchange("/v1/agent/services")
	ResponseEntity<Map<String, Service>> getAgentServices();

	@PutExchange("/v1/agent/service/deregister/{serviceId}")
	ResponseEntity<Void> agentServiceDeregister(@PathVariable String serviceId,
			@RequestHeader(name = ACL_TOKEN_HEADER) String aclToken);

	@PutExchange("/v1/agent/service/register")
	ResponseEntity<Void> agentServiceRegister(@RequestHeader(name = ACL_TOKEN_HEADER, required = false) String aclToken,
			@RequestBody NewService newService);

	@PutExchange("/v1/agent/service/maintenance/{serviceId}")
	ResponseEntity<Void> agentServiceSetMaintenance(@PathVariable String serviceId,
			@RequestParam(required = false) Boolean enable, @RequestParam(required = false) String reason,
			@RequestHeader(name = ACL_TOKEN_HEADER) String aclToken);

	@GetExchange("/v1/catalog/service/{serviceId}")
	ResponseEntity<List<CatalogService>> getCatalogService(@PathVariable String serviceId);

	@GetExchange("/v1/catalog/nodes")
	ResponseEntity<List<Node>> getCatalogNodes();

	@GetExchange("/v1/health/checks/{serviceName}")
	ResponseEntity<List<Check>> getHealthChecksForService(@PathVariable String serviceName);

	@GetExchange("/v1/health/service/{serviceName}")
	ResponseEntity<List<HealthService>> getHealthServices(@PathVariable String serviceName);

	@GetExchange("/v1/health/service/{serviceName}")
	ResponseEntity<List<HealthService>> getHealthServices(@PathVariable String serviceName,
			@RequestParam boolean passing, @RequestHeader(name = ACL_TOKEN_HEADER, required = false) String aclToken,
			@RequestParam(required = false) List<String> tag, QueryParams queryParams);

	@DeleteExchange("/v1/kv/{context}")
	ResponseEntity<Void> deleteKVValues(@PathVariable String context);

	@DeleteExchange("/v1/kv/{context}")
	ResponseEntity<Void> deleteKVValues(@PathVariable String context,
			@RequestHeader(name = ACL_TOKEN_HEADER, required = false) String aclToken);

	@GetExchange("/v1/kv/{context}")
	ResponseEntity<List<GetValue>> getKVValue(@PathVariable String context,
			@RequestHeader(name = ACL_TOKEN_HEADER, required = false) String aclToken);

	@GetExchange("/v1/kv/{context}?recurse")
	ResponseEntity<List<GetValue>> getKVValues(@PathVariable String context,
			@RequestHeader(name = ACL_TOKEN_HEADER, required = false) String aclToken);

	@GetExchange("/v1/kv/{context}?recurse")
	ResponseEntity<List<GetValue>> getKVValues(@PathVariable String context,
			@RequestHeader(name = ACL_TOKEN_HEADER, required = false) String aclToken,
			@RequestParam("wait") @WaitTimeFormat Long waitTime, @RequestParam("index") long index);

	@PutExchange(url = "/v1/kv/{context}", contentType = MediaType.TEXT_PLAIN_VALUE)
	ResponseEntity<Boolean> setKVValue(@PathVariable String context, @RequestBody String value);

	@GetExchange("/v1/events")
	ResponseEntity<List<Event>> eventList();

	@GetExchange("/v1/events")
	ResponseEntity<List<Event>> eventList(int eventTimeout, long index);

	@PostExchange("/v1/event/fire/{name}")
	ResponseEntity<Event> eventFire(@PathVariable String name, @RequestBody String payload);

	class QueryParams {

		private final String datacenter;

		private final ConsistencyMode consistencyMode;

		private final long waitTime;

		private final long index;

		private final String near;

		private QueryParams(String datacenter, ConsistencyMode consistencyMode, long waitTime, long index,
				String near) {
			this.datacenter = datacenter;
			this.consistencyMode = consistencyMode;
			this.waitTime = waitTime;
			this.index = index;
			this.near = near;
		}

		private QueryParams(String datacenter, ConsistencyMode consistencyMode, long waitTime, long index) {
			this(datacenter, consistencyMode, waitTime, index, null);
		}

		public QueryParams(String datacenter) {
			this(datacenter, ConsistencyMode.DEFAULT, -1, -1);
		}

		public QueryParams(ConsistencyMode consistencyMode) {
			this(null, consistencyMode, -1, -1);
		}

		public QueryParams(String datacenter, ConsistencyMode consistencyMode) {
			this(datacenter, consistencyMode, -1, -1);
		}

		public QueryParams(long waitTime, long index) {
			this(null, ConsistencyMode.DEFAULT, waitTime, index);
		}

		public QueryParams(String datacenter, long waitTime, long index) {
			this(datacenter, ConsistencyMode.DEFAULT, waitTime, index, null);
		}

		public String getDatacenter() {
			return datacenter;
		}

		public ConsistencyMode getConsistencyMode() {
			return consistencyMode;
		}

		public long getWaitTime() {
			return waitTime;
		}

		public long getIndex() {
			return index;
		}

		public String getNear() {
			return near;
		}

	}

	enum ConsistencyMode {

		/**
		 * Default Consul Consistency Mode.
		 */
		DEFAULT("default"),

		/**
		 * Stale Consul Consistency Mode.
		 */
		STALE("stale"),

		/**
		 * Consistent Consul Consistency Mode.
		 */
		CONSISTENT("consistent");

		private String paramName;

		ConsistencyMode(String paramName) {
			this.paramName = paramName;
		}

		public String getParamName() {
			return paramName;
		}

		@Override
		public String toString() {
			return this.paramName;
		}

	}

}
