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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.style.ToStringCreator;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties("spring.cloud.consul.retry")
public class RetryProperties {

	/** If consul retry is enabled. */
	private boolean enabled = true;

	/** Initial retry interval in milliseconds. */
	private long initialInterval = 1000;

	/** Multiplier for next interval. */
	private double multiplier = 1.1;

	/** Maximum interval for backoff. */
	private long maxInterval = 2000;

	/** Maximum number of attempts. */
	private int maxAttempts = 6;

	public RetryProperties() {
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public long getInitialInterval() {
		return this.initialInterval;
	}

	public void setInitialInterval(long initialInterval) {
		this.initialInterval = initialInterval;
	}

	public double getMultiplier() {
		return this.multiplier;
	}

	public void setMultiplier(double multiplier) {
		this.multiplier = multiplier;
	}

	public long getMaxInterval() {
		return this.maxInterval;
	}

	public void setMaxInterval(long maxInterval) {
		this.maxInterval = maxInterval;
	}

	public int getMaxAttempts() {
		return this.maxAttempts;
	}

	public void setMaxAttempts(int maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("enabled", this.enabled).append("initialInterval", this.initialInterval)
				.append("multiplier", this.multiplier).append("maxInterval", this.maxInterval)
				.append("maxAttempts", this.maxAttempts).toString();
	}

}
