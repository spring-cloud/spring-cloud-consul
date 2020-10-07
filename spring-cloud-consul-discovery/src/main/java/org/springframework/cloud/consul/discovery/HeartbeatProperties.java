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

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;

import org.apache.commons.logging.Log;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
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

	private static final Log log = org.apache.commons.logging.LogFactory.getLog(HeartbeatProperties.class);

	// TODO: change enabled to default to true when I stop seeing messages like
	// [WARN] agent: Check 'service:testConsulApp:xtest:8080' missed TTL, is now critical
	boolean enabled = false;

	@DurationUnit(ChronoUnit.SECONDS)
	private Duration ttl = Duration.ofSeconds(30);

	@DecimalMin("0.1")
	@DecimalMax("0.9")
	private double intervalRatio = 2.0 / 3.0;

	/**
	 * @return the computed heartbeat interval
	 */
	protected Duration computeHeartbeatInterval() {
		// heartbeat rate at ratio * ttl, but no later than ttl -1s and, (under lesser
		// priority), no sooner than 1s from now
		double interval = this.ttl.getSeconds() * this.intervalRatio;
		double max = Math.max(interval, 1);
		long ttlMinus1 = this.ttl.getSeconds() - 1;
		double min = Math.min(ttlMinus1, max);
		Duration heartbeatInterval = Duration.ofMillis(Math.round(1000 * min));
		log.debug("Computed heartbeatInterval: " + heartbeatInterval);
		return heartbeatInterval;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Duration getTtl() {
		return this.ttl;
	}

	public void setTtl(Duration ttl) {
		this.ttl = ttl;
	}

	public @DecimalMin("0.1") @DecimalMax("0.9") double getIntervalRatio() {
		return this.intervalRatio;
	}

	public void setIntervalRatio(@DecimalMin("0.1") @DecimalMax("0.9") double intervalRatio) {
		this.intervalRatio = intervalRatio;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("enabled", this.enabled).append("ttl", this.ttl)
				.append("intervalRatio", this.intervalRatio).toString();
	}

}
