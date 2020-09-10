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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.ecwid.consul.v1.ConsulClient;

import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.FILES;

public class ConsulConfigDataLocationResolver
		implements ConfigDataLocationResolver<ConsulConfigDataLocation> {

	/**
	 * Consul ConfigData prefix.
	 */
	public static final String PREFIX = "consul:";

	protected static final List<String> DIR_SUFFIXES = Collections.singletonList("/");

	protected static final List<String> FILES_SUFFIXES = Collections
			.unmodifiableList(Arrays.asList(".yml", ".yaml", ".properties"));

	@Override
	public boolean isResolvable(ConfigDataLocationResolverContext context,
			String location) {
		boolean enabled = context.getBinder()
				.bind(ConsulProperties.PREFIX + ".enabled", Boolean.class).orElse(true);
		boolean configEnabled = context.getBinder()
				.bind(ConsulConfigProperties.PREFIX + ".enabled", Boolean.class)
				.orElse(true);
		return location.startsWith(PREFIX) && configEnabled && enabled;
	}

	@Override
	public List<ConsulConfigDataLocation> resolve(
			ConfigDataLocationResolverContext context, String location, boolean optional)
			throws ConfigDataLocationNotFoundException {
		return Collections.emptyList();
	}

	@Override
	public List<ConsulConfigDataLocation> resolveProfileSpecific(
			ConfigDataLocationResolverContext context, String location, boolean optional,
			Profiles profiles) throws ConfigDataLocationNotFoundException {

		UriComponents locationUri = parseLocation(context, location);

		ConsulConfigProperties properties = loadConfigProperties(context.getBinder(),
				locationUri);

		List<String> contexts = (locationUri == null
				|| CollectionUtils.isEmpty(locationUri.getPathSegments()))
						? getAutomaticContexts(profiles, properties)
						: getCustomContexts(locationUri, properties);

		registerBean(context, ConsulClient.class,
				() -> createConsulClient(context, locationUri));

		registerBean(context, ConsulConfigIndexes.class, ConsulConfigDataIndexes::new);

		return contexts.stream()
				.map(propertySourceContext -> new ConsulConfigDataLocation(properties,
						propertySourceContext, optional))
				.collect(Collectors.toList());
	}

	private List<String> getCustomContexts(UriComponents uriComponents,
			ConsulConfigProperties properties) {
		if (StringUtils.isEmpty(uriComponents.getPath())) {
			return Collections.emptyList();
		}

		List<String> contexts = new ArrayList<>();
		for (String path : uriComponents.getPath().split(";")) {
			for (String suffix : getSuffixes(properties)) {
				contexts.add(path + suffix);
			}
		}

		return contexts;
	}

	protected List<String> getSuffixes(ConsulConfigProperties properties) {
		if (properties.getFormat() == FILES) {
			return FILES_SUFFIXES;
		}
		return DIR_SUFFIXES;
	}

	protected List<String> getAutomaticContexts(Profiles profiles,
			ConsulConfigProperties properties) {
		List<String> contexts = new ArrayList<>();

		String prefix = properties.getPrefix();
		String defaultContext = getContext(prefix, properties.getDefaultContext());
		for (String suffix : getSuffixes(properties)) {
			contexts.add(defaultContext + suffix);
		}
		for (String suffix : getSuffixes(properties)) {
			addProfiles(contexts, defaultContext, profiles, suffix, properties);
		}

		// getName() defaults to ${spring.application.name} or application
		String baseContext = getContext(prefix, properties.getName());

		for (String suffix : getSuffixes(properties)) {
			contexts.add(baseContext + suffix);
		}
		for (String suffix : getSuffixes(properties)) {
			addProfiles(contexts, baseContext, profiles, suffix, properties);
		}
		// we build them backwards, first wins, so reverse
		Collections.reverse(contexts);
		return contexts;
	}

	protected String getContext(String prefix, String context) {
		if (StringUtils.isEmpty(prefix)) {
			return context;
		}
		else {
			return prefix + "/" + context;
		}
	}

	protected void addProfiles(List<String> contexts, String baseContext,
			Profiles profiles, String suffix, ConsulConfigProperties properties) {
		for (String profile : profiles.getAccepted()) {
			contexts.add(
					baseContext + properties.getProfileSeparator() + profile + suffix);
		}
	}

	@Nullable
	protected UriComponents parseLocation(ConfigDataLocationResolverContext context,
			String location) {
		String uri = location.substring(PREFIX.length());
		if (!StringUtils.hasText(uri)) {
			return null;
		}
		if (!uri.startsWith("//")) {
			uri = PREFIX + "//" + uri;
		}
		else {
			uri = location;
		}
		return UriComponentsBuilder.fromUriString(uri).build();
	}

	protected <T> void registerBean(ConfigDataLocationResolverContext context,
			Class<T> type, Supplier<T> supplier) {
		context.getBootstrapRegistry().register(type, supplier)
				.onApplicationContextPrepared(
						(ctxt, consulClient) -> ctxt.getBeanFactory().registerSingleton(
								"configData" + type.getSimpleName(), consulClient));
	}

	protected ConsulClient createConsulClient(ConfigDataLocationResolverContext context,
			UriComponents location) {
		ConsulProperties properties = loadProperties(context.getBinder(), location);
		return ConsulAutoConfiguration.createConsulClient(properties);
	}

	protected ConsulProperties loadProperties(Binder binder, UriComponents location) {
		ConsulProperties consulProperties = binder
				.bind(ConsulProperties.PREFIX, Bindable.of(ConsulProperties.class))
				.orElse(new ConsulProperties());

		if (location != null) {
			if (StringUtils.hasText(location.getHost())) {
				consulProperties.setHost(location.getHost());
			}
			if (location.getPort() >= 0) {
				consulProperties.setPort(location.getPort());
			}
		}

		return consulProperties;
	}

	protected ConsulConfigProperties loadConfigProperties(Binder binder,
			UriComponents location) {
		ConsulConfigProperties properties = binder
				.bind(ConsulConfigProperties.PREFIX,
						Bindable.of(ConsulConfigProperties.class))
				.orElse(new ConsulConfigProperties());

		if (StringUtils.isEmpty(properties.getName())) {
			properties.setName(binder.bind("spring.application.name", String.class)
					.orElse("application"));
		}
		return properties;
	}

	protected static class ConsulConfigDataIndexes implements ConsulConfigIndexes {

		private final LinkedHashMap<String, Long> indexes = new LinkedHashMap<>();

		@Override
		public LinkedHashMap<String, Long> getIndexes() {
			return indexes;
		}

	}

}
