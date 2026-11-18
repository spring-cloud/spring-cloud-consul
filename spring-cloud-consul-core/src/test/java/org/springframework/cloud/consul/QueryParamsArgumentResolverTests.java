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

package org.springframework.cloud.consul;

import java.net.URI;

import org.junit.Test;

import org.springframework.cloud.consul.ConsulAutoConfiguration.QueryParamsArgumentResolver;
import org.springframework.cloud.consul.ConsulClient.ConsistencyMode;
import org.springframework.cloud.consul.ConsulClient.QueryParams;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.web.service.invoker.HttpRequestValues;
import org.springframework.web.util.DefaultUriBuilderFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link QueryParamsArgumentResolver}.
 */
public class QueryParamsArgumentResolverTests {

	@Test
	public void staleFlagIsSent() throws Exception {
		assertThat(resolve(new QueryParams(ConsistencyMode.STALE)).getQuery()).isEqualTo("stale=");
	}

	@Test
	public void consistentFlagIsSent() throws Exception {
		assertThat(resolve(new QueryParams(ConsistencyMode.CONSISTENT)).getQuery()).isEqualTo("consistent=");
	}

	@Test
	public void defaultModeSendsNoFlag() throws Exception {
		assertThat(resolve(QueryParams.DEFAULT).getQuery()).isNull();
	}

	private URI resolve(QueryParams params) throws Exception {
		HttpRequestValues.Builder builder = HttpRequestValues.builder()
			.setHttpMethod(HttpMethod.GET)
			.setUriTemplate("/v1/catalog/services");
		MethodParameter parameter = new MethodParameter(getClass().getDeclaredMethod("target", QueryParams.class), 0);
		new QueryParamsArgumentResolver().resolve(params, parameter, builder);
		HttpRequestValues values = builder.build();
		return new DefaultUriBuilderFactory("http://localhost:8500").expand(values.getUriTemplate(),
				values.getUriVariables());
	}

	@Test
	public void consistentModePreservesBlockingQueryParameters() throws Exception {
		URI uri = resolve(new QueryParams(ConsistencyMode.CONSISTENT, 30, 123));

		assertThat(uri.getQuery()).contains("consistent=");
		assertThat(uri.getQuery()).contains("wait=30s");
		assertThat(uri.getQuery()).contains("index=123");
	}

	void target(QueryParams params) {
	}

}
