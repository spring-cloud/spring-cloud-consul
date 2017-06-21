/*
 * Copyright 2013-2016 the original author or authors.
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

import javax.servlet.ServletContext;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.cloud.client.discovery.AbstractDiscoveryLifecycle;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistrationCustomizer;
import org.springframework.cloud.consul.serviceregistry.ConsulServletRegistrationCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import com.ecwid.consul.ConsulException;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Spencer Gibb
 * @author Donnabell Dmello
 * @author Venil Noronha
 *
 * @deprecated See {@link org.springframework.cloud.consul.serviceregistry.ConsulAutoServiceRegistration}
 */
@Slf4j
@Deprecated
public class ConsulLifecycle extends AbstractDiscoveryLifecycle {

	public static final char SEPARATOR = '-';

	private ConsulClient client;

	private ConsulDiscoveryProperties properties;

	private HeartbeatProperties ttlConfig;

	private TtlScheduler ttlScheduler;

	private ObjectProvider<ServletContext> servletContext;

	private NewService service = new NewService();

	private String instanceId;
	private RelaxedPropertyResolver propertyResolver;

	public ConsulLifecycle(ConsulClient client, ConsulDiscoveryProperties properties, HeartbeatProperties ttlConfig) {
		this.client = client;
		this.properties = properties;
		this.ttlConfig = ttlConfig;
	}

	public void setTtlScheduler(TtlScheduler ttlScheduler) {
		this.ttlScheduler = ttlScheduler;
	}

	public void setServletContext(ObjectProvider<ServletContext> servletContext) {
		this.servletContext = servletContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		super.setApplicationContext(applicationContext);
		this.propertyResolver = new RelaxedPropertyResolver(applicationContext.getEnvironment());
	}

	@Override
	protected int getConfiguredPort() {
		return service.getPort() == null? 0 : service.getPort();
	}

	@Override
	protected void setConfiguredPort(int port) {
		service.setPort(port);
	}

	public void setPort(int port) {
		getPort().set(port);
	}

	@Override
	@Retryable(interceptor = "consulRetryInterceptor")
	public void start() {
		super.start();
	}

	@Override
	protected void register() {
		if (!this.properties.isRegister()) {
			log.debug("Registration disabled.");
			return;
		}
		Assert.notNull(service.getPort(), "service.port has not been set");
		List<ConsulRegistrationCustomizer> registrationCustomizers = Collections
				.<ConsulRegistrationCustomizer>singletonList(new ConsulServletRegistrationCustomizer(servletContext));
		ConsulAutoRegistration registration = ConsulAutoRegistration.lifecycleRegistration(service.getPort(),
				getServiceId(), this.properties, getContext(), registrationCustomizers, this.ttlConfig);
		if (registration.getService().getPort() == null) { // not set by properties
			registration.initializePort(service.getPort());
		}
		this.service = registration.getService();

		register(service);
	}

	@Deprecated
	public String getServiceId() {
		return getInstanceId();
	}

	public String getInstanceId() {
		// cache instanceId, so on refresh this won't get recomputed
		// this is a problem if ${random.value} is used
		if (this.instanceId == null) {
			this.instanceId = ConsulAutoRegistration.getInstanceId(properties, getContext());
		}
		return this.instanceId;
	}

	@Override
	protected void registerManagement() {
		if (!this.properties.isRegister()) {
			return;
		}

		ConsulAutoRegistration registration = ConsulAutoRegistration.managementRegistration(this.properties, getContext(), this.ttlConfig);

		register(registration.getService());
	}

	@Override
	protected boolean shouldRegisterManagement() {
		return ConsulAutoRegistration.shouldRegisterManagement(properties, getContext());
	}

	protected void register(NewService newService) {
		log.info("Registering service with consul: {}", newService.toString());
		try {
			client.agentServiceRegister(newService, properties.getAclToken());
			if (ttlConfig.isEnabled() && ttlScheduler != null) {
				ttlScheduler.add(newService);
			}
		}
		catch (ConsulException e) {
			if (this.properties.isFailFast()) {
				log.error("Error registering service with consul: {}", newService.toString(), e);
				ReflectionUtils.rethrowRuntimeException(e);
			}
			log.warn("Failfast is false. Error registering service with consul: {}", newService.toString(), e);
		}
	}

	@Override
	protected Object getConfiguration() {
		return properties;
	}

	@Override
	protected void deregister() {
		deregister(getServiceId());
	}

	@Override
	protected void deregisterManagement() {
		deregister(getManagementServiceId());
	}

	private void deregister(String serviceId) {
		if (!this.properties.isRegister()) {
			return;
		}
		if (ttlScheduler != null) {
			ttlScheduler.remove(serviceId);
		}
		log.info("Deregistering service with consul: {}", serviceId);
		client.agentServiceDeregister(serviceId);
	}

	@Override
	protected boolean isEnabled() {
		return this.properties.getLifecycle().isEnabled();
	}
	
	@Override
	protected String getAppName() {
		return ConsulAutoRegistration.getAppName(this.properties, this.propertyResolver);
	}

	/**
	 * @return the serviceId of the Management Service
	 */
	public String getManagementServiceId() {
		return ConsulAutoRegistration.normalizeForDns(getContext().getId()) + SEPARATOR + properties.getManagementSuffix();
	}

	/**
	 * @return the service name of the Management Service
	 */
	public String getManagementServiceName() {
		return ConsulAutoRegistration.normalizeForDns(getAppName()) + SEPARATOR + properties.getManagementSuffix();
	}

	/**
	 * @return the port of the Management Service
	 */
	protected Integer getManagementPort() {
		return ConsulAutoRegistration.getManagementPort(this.properties, getContext());
	}

	/**
	 * @deprecated See {@link org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration#normalizeForDns(String)}
	 */
	@Deprecated
	public static String normalizeForDns(String s) {
		return ConsulAutoRegistration.normalizeForDns(s);
	}
}
