package org.springframework.cloud.consul.discovery.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.netflix.loadbalancer.Server;
import org.springframework.cloud.consul.discovery.ConsulServer;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.health.model.Check;
import com.netflix.loadbalancer.ServerListFilter;

/**
 * Created by nicu on 12.03.2015.
 */
public class ServiceCheckServerListFilter implements ServerListFilter<Server> {

	private ConsulClient client;

	public ServiceCheckServerListFilter(ConsulClient client) {
		this.client = client;
	}

	@Override
	public List<Server> getFilteredListOfServers(List<Server> servers) {
		Set<String> passingServiceIds = getPassingServiceIds(servers);
		List<Server> okServers = new ArrayList<>(servers.size());
		for (Server server : servers) {
			String serviceId = server.getMetaInfo().getInstanceId();
			if (passingServiceIds.contains(serviceId)) {
                ConsulServer consulServer = ConsulServer.class.cast(server);
				List<Check> nodeChecks = client.getHealthChecksForNode(
						consulServer.getNode(), QueryParams.DEFAULT).getValue();
				boolean passingNodeChecks = true;
				for (Check check : nodeChecks) {
					if (check.getStatus() != Check.CheckStatus.PASSING) {
						passingNodeChecks = false;
						break;
					}
				}
				if (passingNodeChecks) {
					okServers.add(server);
				}
			}
		}
		return okServers;
	}

	private Set<String> getPassingServiceIds(List<Server> servers) {
		Set<String> serviceIds = new HashSet<>(1);
		for (Server server : servers) {
			serviceIds.add(server.getMetaInfo().getInstanceId());
		}
		for (String serviceId : serviceIds) {
			List<Check> serviceChecks = client.getHealthChecksForService(serviceId,
					QueryParams.DEFAULT).getValue();
			for (Check check : serviceChecks) {
				if (check.getStatus() != Check.CheckStatus.PASSING) {
					serviceIds.remove(check.getServiceId());
				}
			}
		}
		return serviceIds;
	}

}
