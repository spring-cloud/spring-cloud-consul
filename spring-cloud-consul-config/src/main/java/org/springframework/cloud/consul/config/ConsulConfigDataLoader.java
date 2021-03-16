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

import com.ecwid.consul.v1.ConsulClient;
import org.apache.commons.logging.Log;

import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.consul.config.ConsulBootstrapper.LoadContext;
import org.springframework.cloud.consul.config.ConsulBootstrapper.LoaderInterceptor;

public class ConsulConfigDataLoader implements ConfigDataLoader<ConsulConfigDataResource> {

	private final Log log;

	public ConsulConfigDataLoader(Log log) {
		this.log = log;
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
			return new ConfigData(Collections.singletonList(propertySource));
		}
		catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Error getting properties from consul: " + resource, e);
			}
			throw new ConfigDataResourceNotFoundException(resource, e);
		}
	}

	protected <T> T getBean(ConfigDataLoaderContext context, Class<T> type) {
		if (context.getBootstrapContext().isRegistered(type)) {
			return context.getBootstrapContext().get(type);
		}
		return null;
	}

}
