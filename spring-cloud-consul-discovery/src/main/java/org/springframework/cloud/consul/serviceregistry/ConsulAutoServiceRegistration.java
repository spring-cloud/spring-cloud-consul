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

package org.springframework.cloud.consul.serviceregistry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * @author Spencer Gibb
 */
public class ConsulAutoServiceRegistration extends AbstractAutoServiceRegistration<ConsulRegistration> {

	private static Log log = LogFactory.getLog(ConsulAutoServiceRegistration.class);

	private ConsulDiscoveryProperties properties;
	private ConsulAutoRegistration registration;

	public ConsulAutoServiceRegistration(ConsulServiceRegistry serviceRegistry,
			AutoServiceRegistrationProperties autoServiceRegistrationProperties,
			ConsulDiscoveryProperties properties, ConsulAutoRegistration registration) {
		super(serviceRegistry, autoServiceRegistrationProperties);
		this.properties = properties;
		this.registration = registration;
	}

	void setPortIfNeeded(int port) {
		getPort().compareAndSet(0, port);
	}

	@Override
	protected ConsulAutoRegistration getRegistration() {
		if (this.registration.getService().getPort() == null && this.getPort().get() > 0) {
			this.registration.initializePort(this.getPort().get());
		}
		Assert.notNull(this.registration.getService().getPort(), "service.port has not been set");
		return this.registration;
	}

	@Override
	protected ConsulAutoRegistration getManagementRegistration() {
		return this.registration.managementRegistration();
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

		super.register();
	}

	@Override
	protected void registerManagement() {
		if (!this.properties.isRegister()) {
			return;
		}
		super.registerManagement();

	}

	@Override
	protected Object getConfiguration() {
		return properties;
	}

	@Override
	protected void deregister() {
		if (!this.properties.isRegister() || !this.properties.isDeregister()) {
			return;
		}
		super.deregister();
	}

	@Override
	protected void deregisterManagement() {
		if (!this.properties.isRegister() || !this.properties.isDeregister()) {
			return;
		}
		super.deregisterManagement();
	}

	@Override
	protected boolean isEnabled() {
		return this.properties.getLifecycle().isEnabled();
	}

	@Override
	@SuppressWarnings("deprecation")
	protected String getAppName() {
		String appName = properties.getServiceName();
		return StringUtils.isEmpty(appName) ? super.getAppName() : appName;
	}

	@Override
	public void bind(WebServerInitializedEvent event) {
		// do nothing so we can listen for this event in a different class
		// this ensures start() can be retried if spring-retry is available
	}
}
