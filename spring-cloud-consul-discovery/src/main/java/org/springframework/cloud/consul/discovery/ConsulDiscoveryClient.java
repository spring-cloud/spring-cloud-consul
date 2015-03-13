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

package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Member;
import com.ecwid.consul.v1.agent.model.Self;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.catalog.model.CatalogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Spencer Gibb
 */
public class ConsulDiscoveryClient implements DiscoveryClient {

    @Autowired
    ApplicationContext context;

    @Autowired
    ConsulClient client;

    @Override
    public String description() {
        return "Spring Cloud Consul Discovery Client";
    }

    @Override
    public ServiceInstance getLocalServiceInstance() {
		Response<Map<String, Service>> agentServices = client.getAgentServices();
		Service service = agentServices.getValue().get(context.getId());
        if (service == null) {
            throw new IllegalStateException("Unable to locate service in consul agent: "+context.getId());
        }
        String host = "localhost";
		Response<Self> agentSelf = client.getAgentSelf();
		Member member = agentSelf.getValue().getMember();
        if (member != null) {
			if (member.getName() != null) {
                host = member.getName();
            }
        }
        return new DefaultServiceInstance(service.getId(), host, service.getPort(), false);
    }

    @Override
    public List<ServiceInstance> getInstances(final String serviceId) {
		List<ServiceInstance> instances = new ArrayList<>();

		addInstancesToList(instances, serviceId);

        return instances;
    }

	private void addInstancesToList(List<ServiceInstance> instances, String serviceId) {
		Response<List<CatalogService>> services = client.getCatalogService(serviceId, QueryParams.DEFAULT);
		for (CatalogService service : services.getValue()) {
            instances.add(new DefaultServiceInstance(serviceId, service.getNode(), service.getServicePort(), false));
		}
	}

    public List<ServiceInstance> getAllInstances() {
        List<ServiceInstance> instances = new ArrayList<>();

		Response<Map<String, List<String>>> services = client.getCatalogServices(QueryParams.DEFAULT);
		for (String serviceId : services.getValue().keySet()) {
			addInstancesToList(instances, serviceId);
		}
		return instances;
    }

    @Override
    public List<String> getServices() {
        return new ArrayList<>(client.getCatalogServices(QueryParams.DEFAULT).getValue().keySet());
    }
}
