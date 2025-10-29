/*
 * Copyright 2013-present the original author or authors.
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

package org.springframework.cloud.consul.model.http;

import org.springframework.http.HttpEntity;
import org.springframework.util.StringUtils;

/**
 * Custom Consul Defined Response Headers.
 *
 * @author Matthew Whitaker
 */
public final class ConsulHeaders {

	private ConsulHeaders() {
	}

	/**
	 * Header name for Consul Index.
	 */
	public static String INDEX_HEADER = "X-Consul-Index";

	/**
	 * Header name for Consul Knownleader.
	 */
	public static String KNOWN_LEADER_HEADER = "X-Consul-Knownleader";

	/**
	 * Header name for Consul Lastcontact.
	 */
	public static String LAST_CONTACT_HEADER = "X-Consul-Lastcontact";

	public static Long getConsulIndex(HttpEntity<?> entity) {
		String header = entity.getHeaders().getFirst(INDEX_HEADER);
		return parseUnsignedLong(header);
	}

	public static Boolean getConsulKnownLeader(HttpEntity<?> entity) {
		String header = entity.getHeaders().getFirst(KNOWN_LEADER_HEADER);
		return parseBoolean(header);
	}

	public static Long getConsulLastContact(HttpEntity<?> entity) {
		String header = entity.getHeaders().getFirst(LAST_CONTACT_HEADER);
		return parseUnsignedLong(header);
	}

	private static Long parseUnsignedLong(String value) {
		if (StringUtils.hasText(value)) {
			try {
				return Long.parseUnsignedLong(value);
			}
			catch (Exception e) {
			}
		}

		return null;
	}

	private static Boolean parseBoolean(String value) {
		if (StringUtils.hasText(value)) {
			if ("true".equals(value)) {
				return true;
			}

			if ("false".equals(value)) {
				return false;
			}
		}

		return null;
	}

}
