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

package org.springframework.cloud.consul.discovery;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.scheduling.annotation.Scheduled;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Spencer Gibb
 */
@Slf4j
public class ConsulCatalogWatch implements ApplicationEventPublisherAware {

	private final ConsulDiscoveryProperties properties;
	private final ConsulClient consul;
	private final AtomicReference<BigInteger> catalogServicesIndex = new AtomicReference<>();
	private ApplicationEventPublisher publisher;

	public ConsulCatalogWatch(ConsulDiscoveryProperties properties, ConsulClient consul) {
		this.properties = properties;
		this.consul = consul;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Scheduled(fixedDelayString = "${spring.cloud.consul.discovery.catalogServicesWatchDelay:30000}")
	public void catalogServicesWatch() {
		try {
			long index = -1;
			if (catalogServicesIndex.get() != null) {
				index = catalogServicesIndex.get().longValue();
			}

			Response<Map<String, List<String>>> response = consul
					.getCatalogServices(new QueryParams(properties
							.getCatalogServicesWatchTimeout(), index));
			Long consulIndex = response.getConsulIndex();
			if (consulIndex != null) {
				catalogServicesIndex.set(BigInteger.valueOf(consulIndex));
			}

			log.trace("Received services update from consul: {}, index: {}",
					response.getValue(), consulIndex);
			publisher.publishEvent(new HeartbeatEvent(this, consulIndex));
		}
		catch (Exception e) {
			log.error("Error watching Consul CatalogServices", e);
		}
	}
}
