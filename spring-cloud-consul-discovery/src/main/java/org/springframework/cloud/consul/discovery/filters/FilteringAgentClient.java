package org.springframework.cloud.consul.discovery.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.cloud.consul.model.SerfStatusEnum;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.Member;

public class FilteringAgentClient {

	private static final int ALIVE_STATUS = SerfStatusEnum.StatusAlive.getCode();

	private final ConsulClient client;

	public FilteringAgentClient(ConsulClient client) {
		this.client = client;
	}

	public List<Member> getAliveAgents() {
		List<Member> members = client.getAgentMembers().getValue();
		List<Member> liveMembers = new ArrayList<>(members.size());
		for (Member peer : members) {
			if (peer.getStatus() == ALIVE_STATUS) {
				liveMembers.add(peer);
			}
		}
		return liveMembers;
	}

	public Set<String> getAliveAgentsAddresses() {
		Set<String> addresses = new HashSet<>();
		for (Member server : getAliveAgents()) {
			addresses.add(server.getAddress());
		}
		return addresses;
	}
}
