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
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataLocationNotFoundException;
import org.springframework.boot.env.BootstrapRegistry.Registration;

import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.FILES;

public class ConsulConfigDataLoader implements ConfigDataLoader<ConsulConfigDataLocation> {

	private static final Log log = LogFactory.getLog(ConsulConfigDataLoader.class);

	@Override
	public ConfigData load(ConfigDataLoaderContext context, ConsulConfigDataLocation location) {
		try {
			ConsulClient consul = getBean(context, ConsulClient.class);
			ConsulConfigProperties properties = location.getProperties();
			ConsulPropertySource propertySource = null;

			if (properties.getFormat() == FILES) {
				Response<GetValue> response = consul.getKVValue(location.getContext(), properties.getAclToken());
				addIndex(context, location, response.getConsulIndex());
				if (response.getValue() != null) {
					ConsulFilesPropertySource filesPropertySource = new ConsulFilesPropertySource(location.getContext(),
							consul, properties);
					filesPropertySource.init(response.getValue());
					propertySource = filesPropertySource;
				}
				else if (!location.isOptional()) {
					throw new ConfigDataLocationNotFoundException(location);
				}
			}
			else {
				propertySource = create(context, location);
			}
			return new ConfigData(Collections.singletonList(propertySource));
		}
		catch (ConfigDataLocationNotFoundException e) {
			throw e;
		}
		catch (Exception e) {
			if (location.getProperties().isFailFast() || !location.isOptional()) {
				throw new ConfigDataLocationNotFoundException(location, e);
			}
			else {
				log.warn("Unable to load consul config from " + location.getContext(), e);
			}
		}
		return null;
	}

	protected <T> T getBean(ConfigDataLoaderContext context, Class<T> type) {
		Registration<T> registration = context.getBootstrapRegistry().getRegistration(type);
		if (registration == null) {
			return null;
		}
		return registration.get();
	}

	protected ConsulPropertySource create(ConfigDataLoaderContext context, ConsulConfigDataLocation location) {
		ConsulPropertySource propertySource = new ConsulPropertySource(location.getContext(),
				getBean(context, ConsulClient.class), location.getProperties());
		propertySource.init();
		addIndex(context, location, propertySource.getInitialIndex());
		return propertySource;
	}

	private void addIndex(ConfigDataLoaderContext context, ConsulConfigDataLocation location, Long consulIndex) {
		ConsulConfigIndexes indexes = getBean(context, ConsulConfigIndexes.class);
		if (indexes != null) { // should never be the case
			indexes.getIndexes().put(location.getContext(), consulIndex);
		}
	}

}
