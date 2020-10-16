/*
 * Copyright 2015-2020 the original author or authors.
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

package org.springframework.cloud.consul.config;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.Test;

import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConsulConfigDataLocationResolverTests {

	@Test
	public void testParseLocation() {
		ConsulConfigDataLocationResolver resolver = new ConsulConfigDataLocationResolver(LogFactory.getLog(getClass()));
		UriComponents uriComponents = resolver.parseLocation(null,
				ConfigDataLocation.of("consul:myhost:8501/mypath1;/mypath2;/mypath3"));
		assertThat(uriComponents.toUri()).hasScheme("consul").hasHost("myhost").hasPort(8501)
				.hasPath("/mypath1;/mypath2;/mypath3");

		uriComponents = resolver.parseLocation(null, ConfigDataLocation.of("consul:myhost:8501"));
		assertThat(uriComponents.toUri()).hasScheme("consul").hasHost("myhost").hasPort(8501).hasPath("");
	}

	@Test
	public void testResolveProfileSpecificWithCustomPaths() {
		String location = "consul:myhost:8501/mypath1;/mypath2;/mypath3";
		List<ConsulConfigDataResource> locations = testResolveProfileSpecific(location);
		assertThat(locations).hasSize(3);
		assertThat(toContexts(locations)).containsExactly("/mypath1/", "/mypath2/", "/mypath3/");
	}

	@Test
	public void testResolveProfileSpecificWithAutomaticPaths() {
		String location = "consul:myhost";
		List<ConsulConfigDataResource> locations = testResolveProfileSpecific(location);
		assertThat(locations).hasSize(4);
		assertThat(toContexts(locations)).containsExactly("config/testapp,dev/", "config/testapp/",
				"config/application,dev/", "config/application/");
	}

	@Test
	public void testLoadProperties() {
		ConsulProperties properties = createResolver().loadProperties(Binder.get(new MockEnvironment()),
				UriComponentsBuilder.fromUriString("consul://myhost:8502").build());
		assertThat(properties.getHost()).isEqualTo("myhost");
		assertThat(properties.getPort()).isEqualTo(8502);
	}

	private List<String> toContexts(List<ConsulConfigDataResource> locations) {
		return locations.stream().map(ConsulConfigDataResource::getContext).collect(Collectors.toList());
	}

	private List<ConsulConfigDataResource> testResolveProfileSpecific(String location) {
		ConsulConfigDataLocationResolver resolver = createResolver();
		ConfigDataLocationResolverContext context = mock(ConfigDataLocationResolverContext.class);
		MockEnvironment env = new MockEnvironment();
		env.setProperty("spring.application.name", "testapp");
		when(context.getBinder()).thenReturn(Binder.get(env));
		Profiles profiles = mock(Profiles.class);
		when(profiles.getAccepted()).thenReturn(Collections.singletonList("dev"));
		return resolver.resolveProfileSpecific(context, ConfigDataLocation.of(location), profiles);
	}

	private ConsulConfigDataLocationResolver createResolver() {
		return new ConsulConfigDataLocationResolver(LogFactory.getLog(getClass())) {
			@Override
			public <T> void registerBean(ConfigDataLocationResolverContext context, Class<T> type, T instance) {
				// do nothing
			}

			@Override
			protected <T> void registerBean(ConfigDataLocationResolverContext context, Class<T> type,
					InstanceSupplier<T> supplier) {
				// do nothing
			}

			@Override
			protected <T> void registerAndPromoteBean(ConfigDataLocationResolverContext context, Class<T> type,
					InstanceSupplier<T> supplier) {
				// do nothing
			}
		};
	}

}
