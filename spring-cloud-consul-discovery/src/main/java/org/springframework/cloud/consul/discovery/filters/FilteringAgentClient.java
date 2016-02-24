/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.consul.discovery.filters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.cloud.consul.model.SerfStatusEnum;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.Member;

@Deprecated
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
