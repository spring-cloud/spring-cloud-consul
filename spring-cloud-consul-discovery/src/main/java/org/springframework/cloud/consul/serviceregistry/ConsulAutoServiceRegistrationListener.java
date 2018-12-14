package org.springframework.cloud.consul.serviceregistry;

import org.springframework.boot.web.context.ConfigurableWebServerApplicationContext;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

public class ConsulAutoServiceRegistrationListener implements SmartApplicationListener {
	private final ConsulAutoServiceRegistration autoServiceRegistration;

	public ConsulAutoServiceRegistrationListener(ConsulAutoServiceRegistration autoServiceRegistration) {
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
				if ("management".equals(
						((ConfigurableWebServerApplicationContext) context).getServerNamespace())) {
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
