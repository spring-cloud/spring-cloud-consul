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

package org.springframework.cloud.consul.config;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties("spring.cloud.consul.config")
@Data
@Validated
public class ConsulConfigProperties {
	private boolean enabled = true;

	private String prefix = "config";

	@NotEmpty
	private String defaultContext = "application";

	@NotEmpty
	private String profileSeparator = ",";

	@NotNull
	private Format format = Format.KEY_VALUE;

	/**
	 * If format is Format.PROPERTIES or Format.YAML
	 * then the following field is used as key to look up consul for configuration.
	 */
	@NotEmpty
	private String dataKey = "data";

	@Value("${consul.token:${CONSUL_TOKEN:${spring.cloud.consul.token:${SPRING_CLOUD_CONSUL_TOKEN:}}}}")
	private String aclToken;

	private Watch watch = new Watch();

	/**
	 * Throw exceptions during config lookup if true, otherwise, log warnings.
	 */
	private boolean failFast = true;

	/**
	 * Alternative to spring.application.name to use in looking up values in consul KV.
	 */
	private String name;

	@PostConstruct
	public void init() {
		if (this.format == Format.FILES) {
			this.profileSeparator = "-";
		}
	}

	@Data
	public static class Watch {
		/** The number of seconds to wait (or block) for watch query, defaults to 55.
		 * Needs to be less than default ConsulClient (defaults to 60). To increase ConsulClient
		 * timeout create a ConsulClient bean with a custom ConsulRawClient with a custom
		 * HttpClient. */
		private int waitTime = 55;

		/** If the watch is enabled. Defaults to true. */
		private boolean enabled = true;

		/** The value of the fixed delay for the watch in millis. Defaults to 1000. */
		private int delay = 1000;
	}

	/**
	 * There are many ways in which we can specify configuration in consul i.e.,
	 *
	 * <ol>
	 * <li>
	 * Nested key value style: Where value is either a constant or part of the key (nested).
	 * For e.g., For following configuration a.b.c=something a.b.d=something else One can
	 * specify the configuration in consul with key as "../kv/config/application/a/b/c" and
	 * value as "something" and key as "../kv/config/application/a/b/d" and value as
	 * "something else"</li>
	 * <li>
	 * Entire contents of properties file as value For e.g., For following configuration
	 * a.b.c=something a.b.d=something else One can specify the configuration in consul with
	 * key as "../kv/config/application/properties" and value as whole configuration "
	 * a.b.c=something a.b.d=something else "</li>
	 * <li>
	 * as Json or YML. You get it.</li>
	 * </ol>
	 *
	 * This enum specifies the different Formats/styles supported for loading the
	 * configuration.
	 *
	 * @author srikalyan.swayampakula
	 */
	public enum Format {
		/**
		 * Indicates that the configuration specified in consul is of type native key values.
		 */
		KEY_VALUE,

		/**
		 * Indicates that the configuration specified in consul is of property style i.e.,
		 * value of the consul key would be a list of key=value pairs separated by new lines.
		 */
		PROPERTIES,

		/**
		 * Indicates that the configuration specified in consul is of YAML style i.e., value
		 * of the consul key would be YAML format
		 */
		YAML,

		/**
		 * Indicates that the configuration specified in consul uses keys as files.
		 * This is useful for tools like git2consul.
		 */
		FILES,

	}
}
