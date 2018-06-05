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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

/**
 * Created by nicu on 11.03.2015.
 * @author Stéphane LEROY
 */
public class TtlScheduler {
	private static final Log log = LogFactory.getLog(ConsulDiscoveryClient.class);

	private final Map<String, ScheduledFuture> serviceHeartbeats = new ConcurrentHashMap<>();

	private final TaskScheduler scheduler = new ConcurrentTaskScheduler(
			Executors.newSingleThreadScheduledExecutor());

	private HeartbeatProperties configuration;

	private ConsulClient client;

	public TtlScheduler(HeartbeatProperties configuration, ConsulClient client) {
		this.configuration = configuration;
		this.client = client;
	}

	@Deprecated
	public void add(final NewService service) {
		add(service.getId());
	}

	/**
	 * Add a service to the checks loop.
	 */
	public void add(String instanceId) {
		ScheduledFuture task = scheduler.scheduleAtFixedRate(new ConsulHeartbeatTask(
				instanceId), configuration.computeHearbeatInterval()
				.toStandardDuration().getMillis());
		ScheduledFuture previousTask = serviceHeartbeats.put(instanceId, task);
		if (previousTask != null) {
			previousTask.cancel(true);
		}
	}

	public void remove(String instanceId) {
		ScheduledFuture task = serviceHeartbeats.get(instanceId);
		if (task != null) {
			task.cancel(true);
		}
		serviceHeartbeats.remove(instanceId);
	}

	private class ConsulHeartbeatTask implements Runnable {
		private String checkId;

		ConsulHeartbeatTask(String serviceId) {
			this.checkId = serviceId;
			if (!checkId.startsWith("service:")) {
				checkId = "service:" + checkId;
			}
		}

		@Override
		public void run() {
			client.agentCheckPass(checkId);
			if (log.isDebugEnabled()) {
				log.debug("Sending consul heartbeat for: " + checkId);
			}
		}
	}
}
