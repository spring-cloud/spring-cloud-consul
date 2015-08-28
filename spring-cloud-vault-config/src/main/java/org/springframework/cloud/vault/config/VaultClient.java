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

import java.util.Collections;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.Setter;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @author Spencer Gibb
 */
@RequiredArgsConstructor
public class VaultClient {
	public static final String VAULT_TOKEN = "X-Vault-Token";

//	protected static final ParameterizedTypeReference<Map<String, String>> typeRef = new ParameterizedTypeReference<Map<String, String>>() { };

	@Setter
	private RestTemplate rest = new RestTemplate();

	private final VaultConfigProperties properties;

	public Map<String, String> read(String key) {
		String url = String.format("%s://%s:%s/v1/{backend}/{key}",
				properties.getScheme(), properties.getHost(), properties.getPort());

		HttpHeaders headers = new HttpHeaders();
		headers.add(VAULT_TOKEN, properties.getToken());
		try {
			ResponseEntity<VaultResponse> response = rest.exchange(url, HttpMethod.GET,
					new HttpEntity<>(headers), VaultResponse.class, properties.getBackend(), key);

			HttpStatus status = response.getStatusCode();
			if (status == HttpStatus.OK) {
				return response.getBody().getData();
			}
		} catch (HttpStatusCodeException e) {
			if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
				return null;
			}
			throw e;
		}

		return Collections.emptyMap();
	}
}
