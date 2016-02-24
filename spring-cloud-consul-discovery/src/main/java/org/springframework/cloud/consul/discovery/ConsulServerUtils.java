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

package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.health.model.HealthService;

import org.springframework.util.StringUtils;

import lombok.extern.apachecommons.CommonsLog;

/**
 * @author Spencer Gibb
 */
@CommonsLog
public class ConsulServerUtils {

	public static String findHost(HealthService healthService) {
		HealthService.Service service = healthService.getService();
		HealthService.Node node = healthService.getNode();

		if (StringUtils.hasText(service.getAddress())) {
			return service.getAddress();
		} else if (StringUtils.hasText(node.getAddress())) {
			return node.getAddress();
		}
		return node.getNode();
	}
}
