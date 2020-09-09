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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import com.ecwid.consul.v1.ConsulClient;

import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.context.config.ConfigDataLocationResolver;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.config.Profiles;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.util.StringUtils;

import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.FILES;

public class ConsulConfigDataLocationResolver
		implements ConfigDataLocationResolver<ConsulConfigDataLocation> {

	@Override
	public boolean isResolvable(ConfigDataLocationResolverContext context,
			String location) {
		boolean enabled = context.getBinder()
				.bind(ConsulProperties.PREFIX + ".enabled", Boolean.class).orElse(true);
		boolean configEnabled = context.getBinder()
				.bind(ConsulConfigProperties.PREFIX + ".enabled", Boolean.class)
				.orElse(true);
		return location.startsWith("consul:") && configEnabled && enabled;
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

		ConsulConfigProperties properties = loadConfigProperties(context.getBinder());

		String appName = properties.getName();
		if (StringUtils.isEmpty(appName)) {
			appName = context.getBinder().bind("spring.application.name", String.class)
					.orElse("application");
		}

		String prefix = properties.getPrefix();
		List<String> suffixes = new ArrayList<>();
		if (properties.getFormat() != FILES) {
			suffixes.add("/");
		}
		else {
			suffixes.add(".yml");
			suffixes.add(".yaml");
			suffixes.add(".properties");
		}

		String defaultContext = getContext(prefix, properties.getDefaultContext());

		List<String> contexts = new ArrayList<>();

		for (String suffix : suffixes) {
			contexts.add(defaultContext + suffix);
		}
		for (String suffix : suffixes) {
			addProfiles(contexts, defaultContext, profiles, suffix, properties);
		}

		String baseContext = getContext(prefix, appName);

		for (String suffix : suffixes) {
			contexts.add(baseContext + suffix);
		}
		for (String suffix : suffixes) {
			addProfiles(contexts, baseContext, profiles, suffix, properties);
		}

		Collections.reverse(contexts);

		// TODO use location for host:port
		ConsulClient consul = createConsulClient(context, location);

		registerBean(context, ConsulClient.class, consul);

		ConsulConfigDataIndexes indexes = new ConsulConfigDataIndexes();

		registerBean(context, ConsulConfigIndexes.class, indexes);

		ArrayList<ConsulConfigDataLocation> locations = new ArrayList<>();
		contexts.forEach(
				propertySourceContext -> locations.add(new ConsulConfigDataLocation(
						properties, propertySourceContext, optional)));

		return locations;
	}

	protected <T> void registerBean(ConfigDataLocationResolverContext context,
			Class<T> type, T instance) {
		context.getBootstrapRegistry().register(type, () -> instance)
				.onApplicationContextPrepared(
						(ctxt, consulClient) -> ctxt.getBeanFactory().registerSingleton(
								"configData" + type.getSimpleName(), consulClient));
	}

	protected ConsulClient createConsulClient(ConfigDataLocationResolverContext context, String location) {
		ConsulProperties properties = loadProperties(context.getBinder());

		String hostPort = location.substring("consul:".length());
		if (StringUtils.hasText(hostPort)) {
			String[] split = hostPort.split(":");
			if (split.length == 2) { // host and port
				properties.setHost(split[0]);
				properties.setPort(Integer.parseInt(split[1]));
			}
		}

		return ConsulAutoConfiguration.createConsulClient(properties);
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

	protected ConsulProperties loadProperties(Binder binder) {
		return binder.bind(ConsulProperties.PREFIX, Bindable.of(ConsulProperties.class))
				.orElse(new ConsulProperties());
	}

	protected ConsulConfigProperties loadConfigProperties(Binder binder) {
		return binder
				.bind(ConsulConfigProperties.PREFIX,
						Bindable.of(ConsulConfigProperties.class))
				.orElse(new ConsulConfigProperties());
	}

	protected static class ConsulConfigDataIndexes implements ConsulConfigIndexes {

		private final LinkedHashMap<String, Long> indexes = new LinkedHashMap<>();

		@Override
		public LinkedHashMap<String, Long> getIndexes() {
			return indexes;
		}

	}

}
