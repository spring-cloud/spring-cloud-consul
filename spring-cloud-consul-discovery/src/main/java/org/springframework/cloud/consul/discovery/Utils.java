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

import com.ecwid.consul.v1.catalog.model.CatalogService;
import org.springframework.util.StringUtils;

/**
 * @author Spencer Gibb
 */
public class Utils {

	public static String getCatalogServiceHost(CatalogService service, boolean preferAddress) {
		if (preferAddress) {
			if (StringUtils.hasText(service.getServiceAddress())) {
				return service.getServiceAddress();
			} else {
				return service.getAddress();
			}
		}
		return service.getNode();
	}

}
