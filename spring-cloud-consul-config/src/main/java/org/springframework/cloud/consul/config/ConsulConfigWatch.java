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
package org.springframework.cloud.consul.config;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.bootstrap.BootstrapApplicationListener;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;

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

	private final AtomicReference<BigInteger> kvIndex = new AtomicReference<>();

	public ConsulConfigWatch(ConsulConfigProperties properties) {
		this.properties = properties;
	}

	@SuppressWarnings("boxing")
	@Scheduled(fixedDelayString = "${spring.cloud.consul.config.kvWatchDelay:10}")
	public void kvWatch() {
		long index = -1;
		if (kvIndex.get() != null) {
			index = kvIndex.get().longValue();
		}

		getConsulPropertySources();
		Set<String> changedValues = new HashSet<String>();
		for (ConsulPropertySource source : consulPropertySources) {
			Response<List<GetValue>> response = source.getSource().getKVValues(source.getContext(),
					new QueryParams(properties.getKvWatchTimeout(), index));
			Long consulIndex = response.getConsulIndex();
			if (consulIndex != null) {
				kvIndex.set(BigInteger.valueOf(consulIndex));
				if (index != consulIndex) {
					if (response.getValue() != null) {
						Set<String> existingProps = new HashSet<String>(Arrays.asList(source.getPropertyNames()));
						for (GetValue getValue : response.getValue()) {
							if (getValue.getModifyIndex() > index) {
								changedValues.add(getValue.getKey());
							}
							existingProps.remove(getValue.getKey().replace(source.getContext(), "").replace("/", "."));
						}
						if (existingProps.size() > 0) {
							changedValues.addAll(existingProps);
						}
					}
				}

				if (changedValues.size() > 0) {
					log.trace("Received kv update from consul: {}, index: {}", changedValues.toString(), kvIndex.get());
					publisher.publishEvent(new EnvironmentChangeEvent(changedValues));
				}
			}
		}
	}

	private void getConsulPropertySources() {
		if (consulPropertySources == null) {
			consulPropertySources = new HashSet<ConsulPropertySource>();
			CompositePropertySource bootstrapPropertySource = ((CompositePropertySource) environment.getPropertySources()
					.get(BootstrapApplicationListener.BOOTSTRAP_PROPERTY_SOURCE_NAME));
			if (bootstrapPropertySource != null) {
				Collection<PropertySource<?>> sources = bootstrapPropertySource.getPropertySources();
				for (PropertySource<?> source : sources) {
					if (source.getName().equals("consul")) {
						CompositePropertySource consulPropertySource = (CompositePropertySource) source;
						for (PropertySource<?> consulSource : consulPropertySource.getPropertySources()) {
							consulPropertySources.add((ConsulPropertySource) consulSource);
						}
						break;
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
