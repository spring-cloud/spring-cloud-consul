package org.springframework.cloud.consul.discovery;

import com.netflix.loadbalancer.Server;
import org.springframework.cloud.netflix.ribbon.DefaultServerIntrospector;

import java.util.Map;

public class ConsulServerIntrospector extends DefaultServerIntrospector {

	@Override
	public boolean isSecure(Server server) {
		Map<String, String> metadata = getMetadata(server);
		if (metadata != null && metadata.containsKey("secure")) {
			return metadata.getOrDefault("secure", "false").equalsIgnoreCase("true");
		}
		return super.isSecure(server);
	}

	@Override
	public Map<String, String> getMetadata(Server server) {
		if (server instanceof ConsulServer) {
			ConsulServer consulServer = (ConsulServer) server;
			return consulServer.getMetadata();
		}
		return super.getMetadata(server);
	}
}
