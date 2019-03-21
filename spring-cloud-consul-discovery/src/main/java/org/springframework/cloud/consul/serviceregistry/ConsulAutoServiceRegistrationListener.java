/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.consul.serviceregistry;

import org.springframework.boot.web.context.ConfigurableWebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

/**
 * Auto registers service upon web server initialization.
 *
 * @author Spencer Gibb
 */
public class ConsulAutoServiceRegistrationListener implements SmartApplicationListener {

	private final ConsulAutoServiceRegistration autoServiceRegistration;

	public ConsulAutoServiceRegistrationListener(
			ConsulAutoServiceRegistration autoServiceRegistration) {
		this.autoServiceRegistration = autoServiceRegistration;
	}

	@Override
	public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
		return WebServerInitializedEvent.class.isAssignableFrom(eventType);
	}

	@Override
	public boolean supportsSourceType(Class<?> sourceType) {
		return true;
	}

	@Override
	public void onApplicationEvent(ApplicationEvent applicationEvent) {
		if (applicationEvent instanceof WebServerInitializedEvent) {
			WebServerInitializedEvent event = (WebServerInitializedEvent) applicationEvent;

			ApplicationContext context = event.getApplicationContext();
			if (context instanceof ConfigurableWebServerApplicationContext) {
				if ("management"
						.equals(((ConfigurableWebServerApplicationContext) context)
								.getServerNamespace())) {
					return;
				}
			}
			this.autoServiceRegistration.setPortIfNeeded(event.getWebServer().getPort());
			this.autoServiceRegistration.start();
		}
	}

	@Override
	public int getOrder() {
		return 0;
	}

}
