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

import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.consul.discovery.filters.ConsulServiceDiscoveryFilter;
import org.springframework.cloud.consul.discovery.filters.TagMatchingDiscoveryFilter;
import org.springframework.cloud.consul.discovery.filters.TagMatchingDiscoveryFilter.TagSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ecwid.consul.v1.ConsulClient;

/**
 * @author Spencer Gibb
 */
@Configuration
@ConditionalOnConsulEnabled
@ConditionalOnProperty(value = "spring.cloud.consul.discovery.enabled", matchIfMissing = true)
@EnableConfigurationProperties
public class ConsulDiscoveryClientConfiguration {

	@Autowired
	private ConsulClient consulClient;

	@Autowired(required = false)
	private ServerProperties serverProperties;

	@Bean
	@ConditionalOnMissingBean
	public ConsulLifecycle consulLifecycle(final ConsulDiscoveryProperties discoveryProperties,
			final HeartbeatProperties heartbeatProperties) {
		return new ConsulLifecycle(consulClient, discoveryProperties, heartbeatProperties);
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnProperty("spring.cloud.consul.discovery.heartbeat.enabled")
	public TtlScheduler ttlScheduler(final HeartbeatProperties heartbeatProperties) {
		return new TtlScheduler(heartbeatProperties, consulClient);
	}

    @Bean
    @ConditionalOnMissingBean
    public ConsulServiceDiscoveryFilter serviceDiscoveryFilter(final ConsulDiscoveryProperties properties) {
    	TagSet included;
    	List<String> includeList = properties.getIncludedTags();
    	if (includeList == null) {
    		included = TagMatchingDiscoveryFilter.MATCH_ALL;
    	} else {
    		included = TagMatchingDiscoveryFilter.wrapSet(new HashSet<>(includeList));
    	}
    	List<String> excludeList = properties.getExcludedTags();
    	TagSet excluded;
    	if (excludeList == null) {
    		excluded = TagMatchingDiscoveryFilter.MATCH_NONE;
    	} else {
    		excluded = TagMatchingDiscoveryFilter.wrapSet(new HashSet<>(excludeList));
    	}
        return new TagMatchingDiscoveryFilter(excluded, included);
    }

	@Bean
	public HeartbeatProperties heartbeatProperties() {
		return new HeartbeatProperties();
	}

	@Bean
	public ConsulDiscoveryProperties consulDiscoveryProperties(final InetUtils inetUtils) {
		return new ConsulDiscoveryProperties(inetUtils);
	}

	@Bean
	@ConditionalOnMissingBean
	public ConsulDiscoveryClient consulDiscoveryClient(final ConsulLifecycle consulLifecycle,
			final ConsulDiscoveryProperties discoveryProperties,
			final ConsulServiceDiscoveryFilter serviceDiscoveryFilter) {
		ConsulDiscoveryClient discoveryClient = new ConsulDiscoveryClient(consulClient,
				consulLifecycle, discoveryProperties, serviceDiscoveryFilter);
		discoveryClient.setServerProperties(serverProperties); //null ok
		return discoveryClient;
	}

	@Bean
	@ConditionalOnMissingBean
	public ConsulCatalogWatch consulCatalogWatch(
			final ConsulDiscoveryProperties discoveryProperties) {
		return new ConsulCatalogWatch(discoveryProperties, consulClient);
	}
}
