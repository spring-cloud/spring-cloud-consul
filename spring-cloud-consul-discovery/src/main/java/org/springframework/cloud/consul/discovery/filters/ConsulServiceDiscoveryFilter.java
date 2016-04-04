/*
 * Copyright 2013-2015 the original author or authors.
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
package org.springframework.cloud.consul.discovery.filters;

import org.springframework.cloud.consul.discovery.ConsulDiscoveryClient;
import org.springframework.cloud.consul.discovery.ConsulServiceInstance;

/**
 * Interface for filtering services discovered by {@link ConsulDiscoveryClient}.
 *
 * @author Adam Hawthorne
 */
public interface ConsulServiceDiscoveryFilter {

	/**
	 * @param instance the instance potentially being filtered
	 * @return {@code true} if the {@link ConsulServiceInstance} should be added to the list of services
	 * discovered by the {@link ConsulDiscoveryClient}, {@code false} if it should not.
	 */
    boolean accept(ConsulServiceInstance instance);

}
