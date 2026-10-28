/*
 * Copyright 2013-present the original author or authors.
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

package org.springframework.cloud.consul.discovery;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.consul.ConsulClient;
import org.springframework.cloud.consul.model.http.agent.NewService;
import org.springframework.cloud.consul.model.http.health.Check.CheckStatus;
import org.springframework.cloud.consul.serviceregistry.ApplicationStatusProvider;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

/**
 * Created by nicu on 11.03.2015.
 *
 * @author Stéphane LEROY
 * @author Chris Bono
 */
public class TtlScheduler {

	private static final Log log = LogFactory.getLog(TtlScheduler.class);

	private final Map<String, ScheduledFuture> serviceHeartbeats = new ConcurrentHashMap<>();

	private final TaskScheduler scheduler = new ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor());

	private final HeartbeatProperties heartbeatProperties;

	private final ConsulDiscoveryProperties discoveryProperties;

	private final ConsulClient client;

	private final ReregistrationPredicate reregistrationPredicate;

	private final Map<String, NewService> registeredServices = new ConcurrentHashMap<>();

	private ApplicationStatusProvider applicationStatusProvider;

	public TtlScheduler(HeartbeatProperties heartbeatProperties, ConsulDiscoveryProperties discoveryProperties,
			ConsulClient client, ReregistrationPredicate reregistrationPredicate,
			ObjectProvider<ApplicationStatusProvider> applicationStatusProviderFactory) {
		this.heartbeatProperties = heartbeatProperties;
		this.discoveryProperties = discoveryProperties;
		this.client = client;
		this.reregistrationPredicate = reregistrationPredicate;
		this.applicationStatusProvider = applicationStatusProviderFactory
			.getIfAvailable(() -> () -> CheckStatus.PASSING);
	}

	public void add(final NewService service) {
		add(service.getId());
		this.registeredServices.put(service.getId(), service);
	}

	/**
	 * Add a service to the checks loop.
	 * @param instanceId instance id
	 */
	public void add(String instanceId) {
		ScheduledFuture task = this.scheduler.scheduleAtFixedRate(
				new ConsulHeartbeatTask(instanceId, this, () -> applicationStatusProvider.currentStatus()),
				this.heartbeatProperties.computeHeartbeatInterval().toMillis());
		ScheduledFuture previousTask = this.serviceHeartbeats.put(instanceId, task);
		if (previousTask != null) {
			previousTask.cancel(true);
		}
	}

	public void remove(String instanceId) {
		ScheduledFuture task = this.serviceHeartbeats.get(instanceId);
		if (task != null) {
			task.cancel(true);
		}
		this.serviceHeartbeats.remove(instanceId);
		this.registeredServices.remove(instanceId);
	}

	static class ConsulHeartbeatTask implements Runnable {

		private final String serviceId;

		private final String checkId;

		private final TtlScheduler ttlScheduler;

		private final Supplier<CheckStatus> statusSupplier;

		ConsulHeartbeatTask(String serviceId, TtlScheduler ttlScheduler, Supplier<CheckStatus> statusSupplier) {
			this.serviceId = serviceId;
			if (!this.serviceId.startsWith("service:")) {
				this.checkId = "service:" + this.serviceId;
			}
			else {
				this.checkId = this.serviceId;
			}
			this.statusSupplier = statusSupplier;
			this.ttlScheduler = ttlScheduler;
		}

		@Override
		public void run() {
			ConsulClient client = this.ttlScheduler.client;
			CheckStatus status = statusSupplier.get();
			switch (status) {
				case PASSING:
					possiblyReregisterIfFails(() -> client.agentCheckPass(checkId, null,
							this.ttlScheduler.discoveryProperties.getAclToken()));
					logHeartbeatSent(status);
					break;
				case WARNING:
					possiblyReregisterIfFails(() -> client.agentCheckWarn(checkId, null,
							this.ttlScheduler.discoveryProperties.getAclToken()));
					logHeartbeatSent(status);
					break;
				case CRITICAL:
					possiblyReregisterIfFails(() -> client.agentCheckFail(checkId, null,
							this.ttlScheduler.discoveryProperties.getAclToken()));
					logHeartbeatSent(status);
					break;
				default:
					log.debug(String.format("Not sending consul heartbeat for %s (%s)", checkId, status));
			}
		}

		private void logHeartbeatSent(CheckStatus status) {
			log.debug(String.format("Sent consul heartbeat for %s (%s)", checkId, status));
		}

		private void possiblyReregisterIfFails(Runnable consulClientCall) {
			try {
				consulClientCall.run();
			}
			catch (ResourceAccessException e) {
				log.warn("Unable to send heartbeat to Consul for service " + this.serviceId
						+ ". Will retry on the next heartbeat. Reason: " + e.getMessage());
				log.debug("Consul heartbeat failed for service " + this.serviceId, e);
			}
			catch (HttpStatusCodeException e) {
				if (this.ttlScheduler.heartbeatProperties.isReregisterServiceOnFailure()
						&& this.ttlScheduler.reregistrationPredicate.isEligible(e)) {
					log.warn(e.getMessage());
					NewService registeredService = this.ttlScheduler.registeredServices.get(this.serviceId);
					if (registeredService != null) {
						if (log.isInfoEnabled()) {
							log.info("Re-register " + registeredService);
						}
						try {
							this.ttlScheduler.client.agentServiceRegister(
									this.ttlScheduler.discoveryProperties.getAclToken(), registeredService);
						}
						catch (RestClientException registrationException) {
							log.warn("Unable to re-register service " + this.serviceId
									+ " with Consul. Will retry on the next heartbeat. Reason: "
									+ registrationException.getMessage());
							log.debug("Consul service re-registration failed for service " + this.serviceId,
									registrationException);
						}
					}
					else {
						log.warn("The service to re-register is not found.");
					}
				}
				else {
					throw e;
				}
			}
		}

	}

}
