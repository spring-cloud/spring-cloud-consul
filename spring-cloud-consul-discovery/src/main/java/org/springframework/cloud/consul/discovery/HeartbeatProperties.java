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

package org.springframework.cloud.consul.discovery;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.joda.time.Period;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.style.ToStringCreator;
import org.springframework.validation.annotation.Validated;

/**
 * Properties related to hearbeat verification.
 *
 * @author Spencer Gibb
 */
@ConfigurationProperties(prefix = "spring.cloud.consul.discovery.heartbeat")
@Validated
public class HeartbeatProperties {

	private static final Log log = org.apache.commons.logging.LogFactory
			.getLog(HeartbeatProperties.class);

	// TODO: change enabled to default to true when I stop seeing messages like
	// [WARN] agent: Check 'service:testConsulApp:xtest:8080' missed TTL, is now critical
	boolean enabled = false;

	@Min(1)
	private int ttlValue = 30;

	@NotNull
	private String ttlUnit = "s";

	@DecimalMin("0.1")
	@DecimalMax("0.9")
	private double intervalRatio = 2.0 / 3.0;

	/**
	 * @deprecated the joda time {@link Period} will be replaced with java8 duration.
	 * @return the computed heartbeat interval
	 */
	@Deprecated
	protected Period computeHearbeatInterval() {
		// heartbeat rate at ratio * ttl, but no later than ttl -1s and, (under lesser
		// priority), no sooner than 1s from now
		double interval = this.ttlValue * this.intervalRatio;
		double max = Math.max(interval, 1);
		int ttlMinus1 = this.ttlValue - 1;
		double min = Math.min(ttlMinus1, max);
		Period heartbeatInterval = new Period(Math.round(1000 * min));
		log.debug("Computed heartbeatInterval: " + heartbeatInterval);
		return heartbeatInterval;
	}

	@Deprecated
	protected Period computeHeartbeatInterval() {
		return computeHearbeatInterval();
	}

	public String getTtl() {
		return this.ttlValue + this.ttlUnit;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public @Min(1) int getTtlValue() {
		return this.ttlValue;
	}

	public void setTtlValue(@Min(1) int ttlValue) {
		this.ttlValue = ttlValue;
	}

	public @NotNull String getTtlUnit() {
		return this.ttlUnit;
	}

	public void setTtlUnit(@NotNull String ttlUnit) {
		this.ttlUnit = ttlUnit;
	}

	public @DecimalMin("0.1") @DecimalMax("0.9") double getIntervalRatio() {
		return this.intervalRatio;
	}

	public void setIntervalRatio(
			@DecimalMin("0.1") @DecimalMax("0.9") double intervalRatio) {
		this.intervalRatio = intervalRatio;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("enabled", this.enabled)
				.append("ttlValue", this.ttlValue).append("ttlUnit", this.ttlUnit)
				.append("intervalRatio", this.intervalRatio).toString();
	}

}
