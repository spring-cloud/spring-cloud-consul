/*
 * Copyright 2013-2019 the original author or authors.
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

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.catalog.CatalogServicesRequest;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

/**
 * @author Spencer Gibb
 */
public class ConsulHealthIndicator extends AbstractHealthIndicator {

	private ConsulClient consul;

	private ConsulHealthIndicatorProperties properties;

	public ConsulHealthIndicator(ConsulClient consul, ConsulHealthIndicatorProperties properties) {
		this.consul = consul;
		this.properties = properties;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) {
		final Response<String> leaderStatus = this.consul.getStatusLeader();
		builder.up().withDetail("leader", leaderStatus.getValue());
		if (properties.isIncludeServicesQuery()) {
			final Response<Map<String, List<String>>> services = this.consul.getCatalogServices(
					CatalogServicesRequest.newBuilder().setQueryParams(QueryParams.DEFAULT).build());
			builder.withDetail("services", services.getValue());
		}
	}

}
