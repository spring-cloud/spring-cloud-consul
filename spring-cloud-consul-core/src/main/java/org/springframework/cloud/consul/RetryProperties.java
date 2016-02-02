/*
 * Copyright 2013-2016 the original author or authors.
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

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties("spring.cloud.consul.retry")
@Data
public class RetryProperties {

	/** Initial retry interval in milliseconds. */
	private long initialInterval = 1000;

	/** Multiplier for next interval. */
	private double multiplier = 1.1;

	/** Maximum interval for backoff. */
	private long maxInterval = 2000;

	/** Maximum number of attempts. */
	private int maxAttempts = 6;
}
