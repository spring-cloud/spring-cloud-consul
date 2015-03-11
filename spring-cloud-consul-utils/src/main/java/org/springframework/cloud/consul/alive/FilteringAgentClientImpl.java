package org.springframework.cloud.consul.alive;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.consul.client.AgentClient;
import org.springframework.cloud.consul.model.Member;
import org.springframework.cloud.consul.model.SerfStatusEnum;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FilteringAgentClientImpl implements FilteringAgentClient {
    public static final int ALIVE_STATUS = SerfStatusEnum.StatusAlive.getCode();
    private final AgentClient agentClient;

    @Autowired
    public FilteringAgentClientImpl(AgentClient agentClient) {
        this.agentClient = agentClient;
    }

    @Override
    public Collection<Member> getAliveAgents() {
        List<Member> members = agentClient.getMembers();
        List<Member> liveMembers = new ArrayList<>(members.size());
        for (Member peer : members) {
            if (peer.getStatus() == ALIVE_STATUS) {
                liveMembers.add(peer);
            }
        }
        return liveMembers;
    }

    @Override
    public Set<String> getAliveAgentsAddresses() {
        Set<String> addresses = new HashSet<String>();
        for (Member server : getAliveAgents()) {
            addresses.add(server.getAddress());
        }
        return addresses;
    }
}
