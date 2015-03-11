package org.springframework.cloud.consul.alive;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.consul.model.SerfStatusEnum;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FilteringAgentClientImpl implements FilteringAgentClient {
    public static final int ALIVE_STATUS = SerfStatusEnum.StatusAlive.getCode();
    private final ConsulClient client;

    @Autowired
    public FilteringAgentClientImpl(ConsulClient client) {
        this.client = client;
    }

    @Override
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

    @Override
    public Set<String> getAliveAgentsAddresses() {
        Set<String> addresses = new HashSet<String>();
        for (Member server : getAliveAgents()) {
            addresses.add(server.getAddress());
        }
        return addresses;
    }
}
