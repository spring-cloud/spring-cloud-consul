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
import java.util.stream.Collectors;

import com.ecwid.consul.v1.ConsulClient;
import org.apache.commons.logging.Log;

import org.springframework.boot.BootstrapContext;
import org.springframework.boot.BootstrapRegistry.InstanceSupplier;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.ConfigDataLocation;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.FILES;

public class ConsulConfigDataLocationResolver implements ConfigDataLocationResolver<ConsulConfigDataResource> {

	/**
	 * Consul ConfigData prefix.
	 */
	public static final String PREFIX = "consul:";

	protected static final List<String> DIR_SUFFIXES = Collections.singletonList("/");

	protected static final List<String> FILES_SUFFIXES = Collections
			.unmodifiableList(Arrays.asList(".yml", ".yaml", ".properties"));

	private final Log log;

	public ConsulConfigDataLocationResolver(Log log) {
		this.log = log;
	}

	@Override
	public boolean isResolvable(ConfigDataLocationResolverContext context, ConfigDataLocation location) {
		if (!location.hasPrefix(PREFIX)) {
			return false;
		}
		// only bind if correct prefix
		boolean enabled = context.getBinder().bind(ConsulProperties.PREFIX + ".enabled", Boolean.class).orElse(true);
		boolean configEnabled = context.getBinder().bind(ConsulConfigProperties.PREFIX + ".enabled", Boolean.class)
				.orElse(true);
		return configEnabled && enabled;
	}

	@Override
	public List<ConsulConfigDataResource> resolve(ConfigDataLocationResolverContext context,
			ConfigDataLocation location) throws ConfigDataLocationNotFoundException {
		return Collections.emptyList();
	}

	@Override
	public List<ConsulConfigDataResource> resolveProfileSpecific(ConfigDataLocationResolverContext resolverContext,
			ConfigDataLocation location, Profiles profiles) throws ConfigDataLocationNotFoundException {
		UriComponents locationUri = parseLocation(resolverContext, location);

		// create consul client
		registerBean(resolverContext, ConsulProperties.class, loadProperties(resolverContext.getBinder(), locationUri));

		registerAndPromoteBean(resolverContext, ConsulClient.class, this::createConsulClient);

		// create locations
		ConsulConfigProperties properties = loadConfigProperties(resolverContext.getBinder());

		ConsulPropertySources consulPropertySources = new ConsulPropertySources(properties, log);

		List<String> contexts = (locationUri == null || CollectionUtils.isEmpty(locationUri.getPathSegments()))
				? consulPropertySources.getAutomaticContexts(profiles.getAccepted())
				: getCustomContexts(locationUri, properties);

		registerAndPromoteBean(resolverContext, ConsulConfigProperties.class, InstanceSupplier.of(properties));

		registerAndPromoteBean(resolverContext, ConsulConfigIndexes.class,
				InstanceSupplier.from(ConsulConfigDataIndexes::new));

		return contexts.stream().map(propertySourceContext -> new ConsulConfigDataResource(propertySourceContext,
				location.isOptional(), properties, consulPropertySources)).collect(Collectors.toList());
	}

	private List<String> getCustomContexts(UriComponents uriComponents, ConsulConfigProperties properties) {
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

	@Nullable
	protected UriComponents parseLocation(ConfigDataLocationResolverContext context, ConfigDataLocation location) {
		String originalLocation = location.getNonPrefixedValue(PREFIX);
		if (!StringUtils.hasText(originalLocation)) {
			return null;
		}
		String uri;
		if (!originalLocation.startsWith("//")) {
			uri = PREFIX + "//" + originalLocation;
		}
		else {
			uri = originalLocation;
		}
		return UriComponentsBuilder.fromUriString(uri).build();
	}

	protected <T> void registerAndPromoteBean(ConfigDataLocationResolverContext context, Class<T> type,
			InstanceSupplier<T> supplier) {
		registerBean(context, type, supplier);
		context.getBootstrapContext().addCloseListener(event -> {
			T instance = event.getBootstrapContext().get(type);
			String name = "configData" + type.getSimpleName();
			ConfigurableApplicationContext appCtxt = event.getApplicationContext();
			if (!appCtxt.containsBean(name)) {
				appCtxt.getBeanFactory().registerSingleton(name, instance);
			}
		});
	}

	public <T> void registerBean(ConfigDataLocationResolverContext context, Class<T> type, T instance) {
		context.getBootstrapContext().registerIfAbsent(type, InstanceSupplier.of(instance));
	}

	protected <T> void registerBean(ConfigDataLocationResolverContext context, Class<T> type,
			InstanceSupplier<T> supplier) {
		ConfigurableBootstrapContext bootstrapContext = context.getBootstrapContext();
		bootstrapContext.registerIfAbsent(type, supplier);
	}

	protected ConsulClient createConsulClient(BootstrapContext context) {
		ConsulProperties properties = context.get(ConsulProperties.class);

		return ConsulAutoConfiguration.createConsulClient(properties);
	}

	protected ConsulProperties loadProperties(Binder binder, UriComponents location) {
		ConsulProperties consulProperties = binder.bind(ConsulProperties.PREFIX, Bindable.of(ConsulProperties.class))
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

	protected ConsulConfigProperties loadConfigProperties(Binder binder) {
		ConsulConfigProperties properties = binder
				.bind(ConsulConfigProperties.PREFIX, Bindable.of(ConsulConfigProperties.class))
				.orElse(new ConsulConfigProperties());

		if (StringUtils.isEmpty(properties.getName())) {
			properties.setName(binder.bind("spring.application.name", String.class).orElse("application"));
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
