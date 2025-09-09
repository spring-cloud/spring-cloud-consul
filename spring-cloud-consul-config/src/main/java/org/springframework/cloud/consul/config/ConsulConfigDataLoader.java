/*
 * Copyright 2015-present the original author or authors.
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
import java.util.EnumSet;
import java.util.List;

import com.ecwid.consul.v1.ConsulClient;
import org.apache.commons.logging.Log;

import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigData.Option;
import org.springframework.boot.context.config.ConfigData.Options;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.cloud.consul.config.ConsulBootstrapper.LoadContext;
import org.springframework.cloud.consul.config.ConsulBootstrapper.LoaderInterceptor;
import org.springframework.util.StringUtils;

public class ConsulConfigDataLoader implements ConfigDataLoader<ConsulConfigDataResource> {

	private static final EnumSet<Option> ALL_OPTIONS = EnumSet.allOf(Option.class);

	private final Log log;

	public ConsulConfigDataLoader(DeferredLogFactory logFactory) {
		this.log = logFactory.getLog(ConsulConfigDataLoader.class);
	}

	@Override
	public ConfigData load(ConfigDataLoaderContext context, ConsulConfigDataResource resource) {
		if (context.getBootstrapContext().isRegistered(LoaderInterceptor.class)) {
			LoaderInterceptor interceptor = context.getBootstrapContext().get(LoaderInterceptor.class);
			if (interceptor != null) {
				Binder binder = context.getBootstrapContext().get(Binder.class);
				return interceptor.apply(new LoadContext(context, resource, binder, this::doLoad));
			}
		}
		return doLoad(context, resource);
	}

	public ConfigData doLoad(ConfigDataLoaderContext context, ConsulConfigDataResource resource) {
		try {
			ConsulClient consul = getBean(context, ConsulClient.class);
			ConsulConfigIndexes indexes = getBean(context, ConsulConfigIndexes.class);

			ConsulPropertySource propertySource = resource.getConsulPropertySources()
				.createPropertySource(resource.getContext(), consul, indexes.getIndexes()::put);
			if (propertySource == null) {
				return null;
			}
			List<ConsulPropertySource> propertySources = Collections.singletonList(propertySource);
			if (ALL_OPTIONS.size() == 1) {
				// boot 2.4.2 and prior
				return new ConfigData(propertySources);
			}
			else if (ALL_OPTIONS.size() == 2) {
				// boot 2.4.3 and 2.4.4
				return new ConfigData(propertySources, Option.IGNORE_IMPORTS, Option.IGNORE_PROFILES);
			}
			else if (ALL_OPTIONS.size() > 2) {
				// boot 2.4.5+
				return new ConfigData(propertySources, source -> {
					List<Option> options = new ArrayList<>();
					options.add(Option.IGNORE_IMPORTS);
					options.add(Option.IGNORE_PROFILES);
					if (StringUtils.hasText(resource.getProfile())) {
						options.add(Option.PROFILE_SPECIFIC);
					}
					return Options.of(options.toArray(new Option[0]));
				});
			}
		}
		catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Error getting properties from consul: " + resource, e);
			}
			throw new ConfigDataResourceNotFoundException(resource, e);
		}
		return null;
	}

	protected <T> T getBean(ConfigDataLoaderContext context, Class<T> type) {
		if (context.getBootstrapContext().isRegistered(type)) {
			return context.getBootstrapContext().get(type);
		}
		return null;
	}

}
