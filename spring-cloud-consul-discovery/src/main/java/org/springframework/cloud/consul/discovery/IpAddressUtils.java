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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;

import org.springframework.util.StringUtils;

import com.ecwid.consul.v1.catalog.model.CatalogService;

/**
 * @author Spencer Gibb
 */
@CommonsLog
public class IpAddressUtils {

	public static String getCatalogServiceHost(CatalogService service) {
		if (StringUtils.hasText(service.getServiceAddress())) {
			return service.getServiceAddress();
		} else if (StringUtils.hasText(service.getAddress())) {
			return service.getAddress();
		}
		return service.getNode();
	}
}
