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

import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;

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
		final String leaderStatus = this.consul.getStatusLeader();
		builder.up().withDetail("leader", leaderStatus);
		if (properties.isIncludeServicesQuery()) {
			final Map<String, List<String>> services = this.consul.getCatalogServices();
			builder.withDetail("services", services);
		}
	}

}
