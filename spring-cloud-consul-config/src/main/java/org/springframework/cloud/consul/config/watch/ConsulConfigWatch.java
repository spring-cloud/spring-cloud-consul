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
package org.springframework.cloud.consul.config.watch;

import static org.springframework.util.Base64Utils.decodeFromString;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.cloud.bootstrap.config.PropertySourceBootstrapConfiguration;
import org.springframework.cloud.consul.config.ConsulConfigProperties;
import org.springframework.cloud.consul.config.ConsulPropertySource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.ConcurrentReferenceHashMap;

import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;

import lombok.extern.slf4j.Slf4j;

/**
 * A kv store watch that publishes an EnvironmentChangeEvent whenever a value under one of the property sources is changed
 *
 * @author Andrew DePompa
 */
@Slf4j
public class ConsulConfigWatch implements ApplicationEventPublisherAware, EnvironmentAware {

	private ApplicationEventPublisher publisher;

	private ConfigurableEnvironment environment;

	private Set<ConsulPropertySource> consulPropertySources = null;

	private ConsulConfigProperties properties;

	private Map<String, BigInteger> kvIndexes = new ConcurrentReferenceHashMap<String, BigInteger>();

	// used to keep track of existing props in case one is deleted
	private Set<String> existingProps;

	public ConsulConfigWatch(ConsulConfigProperties properties) {
		this.properties = properties;
	}

	@SuppressWarnings("boxing")
	@Scheduled(fixedDelayString = "${spring.cloud.consul.config.kvWatchDelay:10}")
	public void kvWatch() {
		findConsulPropertySources();
		Map<String, String> changedProps = new HashMap<>();
		for (ConsulPropertySource source : consulPropertySources) {
			long index = -1;
			if(kvIndexes.get(source.getName()) != null) {
				index = kvIndexes.get(source.getName()).longValue();
			}
			Response<List<GetValue>> response = source.getSource().getKVValues(source.getContext(),
					new QueryParams(properties.getKvWatchTimeout(), index));
			Long consulIndex = response.getConsulIndex();
			if (consulIndex != null) {
				Set<String> deletedProps = new HashSet<String>(existingProps);
				kvIndexes.put(source.getName(), BigInteger.valueOf(consulIndex));
				if (index != consulIndex) {
					if (response.getValue() != null) {
						for (GetValue getValue : response.getValue()) {
							if (getValue.getModifyIndex() > index) {
								changedProps.put(getValue.getKey(), getValue.getValue() == null ? "" : new String(decodeFromString(getValue.getValue())));
								existingProps.add(getValue.getKey().replace(source.getContext(), "").replace("/", "."));
							}
							deletedProps.remove(getValue.getKey().replace(source.getContext(), "").replace("/", "."));
						}
						for(String key : deletedProps){
							changedProps.put(key, null);
							existingProps.remove(key.replace(source.getContext(), "").replace("/", "."));
						}
					}
				}
			}
		}
		if (changedProps.size() > 0) {
			log.trace("Received kv update from consul: {}", changedProps.toString());
			publisher.publishEvent(new ConsulKeyValueChangeEvent(changedProps));
		}
	}

	private void findConsulPropertySources() {
		if (consulPropertySources == null) {
			if(existingProps == null) {
				existingProps = new HashSet<String>();
			}
			consulPropertySources = new HashSet<ConsulPropertySource>();
			if (environment.getPropertySources()
					.get(PropertySourceBootstrapConfiguration.BOOTSTRAP_PROPERTY_SOURCE_NAME) instanceof CompositePropertySource) {
				CompositePropertySource bootstrapPropertySource = ((CompositePropertySource) environment.getPropertySources()
						.get(PropertySourceBootstrapConfiguration.BOOTSTRAP_PROPERTY_SOURCE_NAME));
				if (bootstrapPropertySource != null) {
					Collection<PropertySource<?>> sources = bootstrapPropertySource.getPropertySources();
					for (PropertySource<?> source : sources) {
						if (source.getName().equals("consul")) {
							CompositePropertySource consulPropertySource = (CompositePropertySource) source;
							for (PropertySource<?> consulSource : consulPropertySource.getPropertySources()) {
								consulPropertySources.add((ConsulPropertySource) consulSource);
								existingProps.addAll(Arrays.asList(((ConsulPropertySource) consulSource).getPropertyNames()));
							}
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public void setEnvironment(Environment environment) {
		if (environment instanceof ConfigurableEnvironment) {
			this.environment = (ConfigurableEnvironment) environment;
		}
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}
}
