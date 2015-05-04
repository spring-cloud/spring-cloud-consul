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

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Spencer Gibb
 */
@Configuration
public class ConsulDiscoveryClientConfiguration {

	@Autowired
	private ConsulClient consulClient;

    @Autowired
    private TaskScheduler scheduler;

    @Bean
	public ConsulLifecycle consulLifecycle() {
		return new ConsulLifecycle();
	}

    @Bean
    public TaskScheduler taskScheduler(){
        return new ConcurrentTaskScheduler();
    }

    @Bean
	public HeartbeatProperties heartbeatProperties() {
		return new HeartbeatProperties();
	}

	@Bean
	public ConsulDiscoveryClient consulDiscoveryClient() {
		return new ConsulDiscoveryClient();
	}

    @Bean
    public TtlScheduler ttlScheduler(Map<String, HealthIndicator> healthIndicators) {
        return new TtlScheduler(scheduler, heartbeatProperties(), consulClient, summaryHealthIndicator(healthIndicators));
    }

    private static HealthIndicator summaryHealthIndicator(final Map<String, HealthIndicator> healthIndicators) {
        return new HealthIndicator() {
            @Override
            public Health health() {
                Map<String, Health> currentStatuses = new HashMap<>();
                for (Map.Entry<String, HealthIndicator> indicatorEntry : healthIndicators.entrySet()) {
                    currentStatuses.put(indicatorEntry.getKey(), indicatorEntry.getValue().health());
                }
                return summaryHealthAggregator().aggregate(currentStatuses);
            }
        };
    }

    private static HealthAggregator summaryHealthAggregator() {
        OrderedHealthAggregator orderedHealthAggregator = new OrderedHealthAggregator();
        //todo set these by configuring
        orderedHealthAggregator.setStatusOrder(Status.DOWN, Status.OUT_OF_SERVICE, Status.UNKNOWN, Status.UP);
        return orderedHealthAggregator;
    }

}
