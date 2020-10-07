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

import com.ecwid.consul.v1.ConsulClient;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.style.ToStringCreator;

/**
 * Configuration properties for {@link ConsulHealthIndicator}.
 *
 * @author Chris Bono
 */
@ConfigurationProperties("spring.cloud.consul.health-indicator")
public class ConsulHealthIndicatorProperties {

	/**
	 * Whether or not the indicator should include a query for all registered services
	 * during its execution. When set to {@code false} the indicator only uses the lighter
	 * {@link ConsulClient#getStatusLeader()}. This can be helpful in large deployments
	 * where the number of services returned makes the operation unnecessarily heavy.
	 */
	private boolean includeServicesQuery = true;

	boolean isIncludeServicesQuery() {
		return includeServicesQuery;
	}

	void setIncludeServicesQuery(boolean includeServicesQuery) {
		this.includeServicesQuery = includeServicesQuery;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("includeServicesQuery", this.includeServicesQuery).toString();
	}

}
