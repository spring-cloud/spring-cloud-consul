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

package org.springframework.cloud.vault.config;

import lombok.Data;

import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties("spring.cloud.vault")
@Data
public class VaultConfigProperties {
	private boolean enabled = true;

	@NotEmpty
	private String host = "127.0.0.1";

	@Range(min = 1, max = 65535)
	private int port = 8200;

	private String scheme = "http";

	@NotEmpty
	private String backend = "secret";

	@NotEmpty
	private String defaultContext = "application";

	@NotEmpty
	private String profileSeparator = ",";

	@NotEmpty
	private String token;
}
