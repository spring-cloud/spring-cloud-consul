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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.ecwid.consul.v1.health.model.HealthService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.util.StringUtils;

/**
 * @author Spencer Gibb
 * @author Semenkov Alexey
 */
public final class ConsulServerUtils {

	private static final Log log = LogFactory.getLog(ConsulServerUtils.class);

	private ConsulServerUtils() {
		throw new IllegalStateException("Can't instantiate a utility class");
	}

	public static String findHost(HealthService healthService) {
		HealthService.Service service = healthService.getService();
		HealthService.Node node = healthService.getNode();

		if (StringUtils.hasText(service.getAddress())) {
			return fixIPv6Address(service.getAddress());
		}
		else if (StringUtils.hasText(node.getAddress())) {
			return fixIPv6Address(node.getAddress());
		}
		return node.getNode();
	}

	public static String fixIPv6Address(String address) {
		try {
			InetAddress inetAdr = InetAddress.getByName(address);
			if (inetAdr instanceof Inet6Address) {
				return "[" + inetAdr.getHostAddress() + "]";
			}
			return address;
		}
		catch (UnknownHostException e) {
			log.debug("Not InetAddress: " + address + " , resolved as is.");
			return address;
		}
	}

}
