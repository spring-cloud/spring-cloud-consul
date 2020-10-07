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
import java.util.List;
import java.util.function.BiConsumer;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;
import org.apache.commons.logging.Log;

import org.springframework.util.StringUtils;

import static org.springframework.cloud.consul.config.ConsulConfigProperties.Format.FILES;

public class ConsulPropertySources {

	protected static final List<String> DIR_SUFFIXES = Collections.singletonList("/");

	protected static final List<String> FILES_SUFFIXES = Collections
			.unmodifiableList(Arrays.asList(".yml", ".yaml", ".properties"));

	private final ConsulConfigProperties properties;

	private final Log log;

	public ConsulPropertySources(ConsulConfigProperties properties, Log log) {
		this.properties = properties;
		this.log = log;
	}

	public List<String> getAutomaticContexts(List<String> profiles) {
		List<String> contexts = new ArrayList<>();
		String prefix = properties.getPrefix();
		String defaultContext = getContext(prefix, properties.getDefaultContext());
		List<String> suffixes = getSuffixes();
		for (String suffix : suffixes) {
			contexts.add(defaultContext + suffix);
		}
		for (String suffix : suffixes) {
			addProfiles(contexts, defaultContext, profiles, suffix);
		}

		// getName() defaults to ${spring.application.name} or application
		String baseContext = getContext(prefix, properties.getName());

		for (String suffix : suffixes) {
			contexts.add(baseContext + suffix);
		}
		for (String suffix : suffixes) {
			addProfiles(contexts, baseContext, profiles, suffix);
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

	protected List<String> getSuffixes() {
		if (properties.getFormat() == FILES) {
			return FILES_SUFFIXES;
		}
		return DIR_SUFFIXES;
	}

	private void addProfiles(List<String> contexts, String baseContext, List<String> profiles, String suffix) {
		for (String profile : profiles) {
			contexts.add(baseContext + properties.getProfileSeparator() + profile + suffix);
		}
	}

	public ConsulPropertySource createPropertySource(String propertySourceContext, boolean optional,
			ConsulClient consul, BiConsumer<String, Long> indexConsumer) {
		try {
			ConsulPropertySource propertySource = null;

			if (properties.getFormat() == FILES) {
				Response<GetValue> response = consul.getKVValue(propertySourceContext, properties.getAclToken());
				indexConsumer.accept(propertySourceContext, response.getConsulIndex());
				if (response.getValue() != null) {
					ConsulFilesPropertySource filesPropertySource = new ConsulFilesPropertySource(propertySourceContext,
							consul, properties);
					filesPropertySource.init(response.getValue());
					propertySource = filesPropertySource;
				}
				else if (!optional) {
					throw new PropertySourceNotFoundException(propertySourceContext);
				}
			}
			else {
				propertySource = create(propertySourceContext, consul, indexConsumer);
			}
			return propertySource;
		}
		catch (PropertySourceNotFoundException e) {
			throw e;
		}
		catch (Exception e) {
			if (properties.isFailFast() || !optional) {
				throw new PropertySourceNotFoundException(propertySourceContext, e);
			}
			else {
				log.warn("Unable to load consul config from " + propertySourceContext, e);
			}
		}
		return null;
	}

	private ConsulPropertySource create(String context, ConsulClient consulClient,
			BiConsumer<String, Long> indexConsumer) {
		ConsulPropertySource propertySource = new ConsulPropertySource(context, consulClient, this.properties);
		propertySource.init();
		indexConsumer.accept(context, propertySource.getInitialIndex());
		return propertySource;
	}

	static class PropertySourceNotFoundException extends RuntimeException {

		private final String context;

		PropertySourceNotFoundException(String context) {
			this.context = context;
		}

		PropertySourceNotFoundException(String context, Exception cause) {
			super(cause);
			this.context = context;
		}

		public String getContext() {
			return this.context;
		}

	}

}
