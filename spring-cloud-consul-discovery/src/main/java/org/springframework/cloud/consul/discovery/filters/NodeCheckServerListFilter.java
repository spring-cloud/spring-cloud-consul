package org.springframework.cloud.consul.discovery.filters;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.health.model.Check;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerListFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NodeCheckServerListFilter implements ServerListFilter<Server> {

	private ConsulClient client;

	public NodeCheckServerListFilter(ConsulClient client) {
		this.client = client;
	}

	private Set<String> nonPassingServiceInstancesByHealth(String serviceName) {
		List<Check> checks = client.getHealthChecksForService(serviceName,
				QueryParams.DEFAULT).getValue();
		// now we filter by service instances' health
		Set<String> nonPassingInstances = new HashSet<>();
		for (Check check : checks) {
			if (!check.getStatus().equals(Check.CheckStatus.PASSING)) {
				nonPassingInstances.add(check.getServiceId());
			}
		}
		return nonPassingInstances;
	}

    @Override
	public List<Server> getFilteredListOfServers(List<Server> servers) {
		if (servers.isEmpty())
			return servers;
		List<Server> okServers = new ArrayList<>();
		Set<String> nonPassingServiceInstancesByHealth = nonPassingServiceInstancesByHealth(servers
				.get(0).getMetaInfo().getAppName());
		for (Server server : servers) {
			if (!nonPassingServiceInstancesByHealth.contains(server.getMetaInfo()
					.getInstanceId())) {
				okServers.add(server);
			}
		}
		return null;
	}
}
