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

package org.springframework.cloud.consul.model.http.kv;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetValue {

	@JsonProperty("Key")
	private String key;

	@JsonProperty("Value")
	private String value;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDecodedValue(Charset charset) {
		if (this.value == null) {
			return null;
		}
		else {
			if (charset == null) {
				charset = StandardCharsets.UTF_8;
			}

			return new String(Base64.getDecoder().decode(this.value), charset);
		}
	}

	public String getDecodedValue() {
		return this.getDecodedValue(StandardCharsets.UTF_8);
	}

}
