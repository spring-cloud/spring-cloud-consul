package org.springframework.cloud.consul.config;

import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsulConfigurationListener implements ApplicationListener<EnvironmentChangeEvent> {

	private RefreshEndpoint refreshEndpoint;

	public ConsulConfigurationListener(RefreshEndpoint refreshEndpoint) {
		this.refreshEndpoint = refreshEndpoint;
	}

	@Override
	public void onApplicationEvent(EnvironmentChangeEvent event) {
		log.trace("Received EnvironmentChangeEvent. Refreshing...");
		refreshEndpoint.refresh();
	}
}
