package org.springframework.cloud.consul.config.watch;

import java.util.Map;

import org.springframework.context.ApplicationEvent;

public class ConsulKeyValueChangeEvent extends ApplicationEvent {

	private static final long serialVersionUID = 1L;

	private Map<String, String> properties; 
	
	public ConsulKeyValueChangeEvent(Map<String, String> source) {
		super(source);
		properties = source;
	}

	public Map<String, String> getProperties(){
		return properties;
	}
}
