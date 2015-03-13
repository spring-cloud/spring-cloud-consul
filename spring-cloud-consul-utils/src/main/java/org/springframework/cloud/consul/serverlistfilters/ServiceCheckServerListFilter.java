package org.springframework.cloud.consul.serverlistfilters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.consul.discovery.ConsulServer;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.health.model.Check;
import com.netflix.loadbalancer.ServerListFilter;

/**
 * Created by nicu on 12.03.2015.
 */
public class ServiceCheckServerListFilter implements ServerListFilter<ConsulServer> {

	@Autowired
	private ConsulClient client;

	@Override
	public List<ConsulServer> getFilteredListOfServers(List<ConsulServer> servers) {
		Set<String> passingServiceIds = getPassingServiceIds(servers);
		List<ConsulServer> okServers = new ArrayList<>(servers.size());
		for (ConsulServer consulServer : servers) {
			String serviceId = consulServer.getMetaInfo().getInstanceId();
			if (passingServiceIds.contains(serviceId)) {
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
					okServers.add(consulServer);
				}
			}
		}
		return okServers;
	}

	private Set<String> getPassingServiceIds(List<ConsulServer> servers) {
		Set<String> serviceIds = new HashSet<>(1);
		for (ConsulServer server : servers) {
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
