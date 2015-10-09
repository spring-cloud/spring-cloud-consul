package org.springframework.cloud.consul.config.watch;

import org.springframework.cloud.endpoint.RefreshEndpoint;
import org.springframework.context.ApplicationListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConsulConfigurationListener implements ApplicationListener<ConsulKeyValueChangeEvent> {

	private RefreshEndpoint refreshEndpoint;

	public ConsulConfigurationListener(RefreshEndpoint refreshEndpoint) {
		this.refreshEndpoint = refreshEndpoint;
	}

	@Override
	public void onApplicationEvent(ConsulKeyValueChangeEvent event) {
		log.trace("Received ConsulKeyValueChangeEvent. Refreshing...");
		refreshEndpoint.refresh();
	}
}
