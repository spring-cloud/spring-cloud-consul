package org.springframework.cloud.consul.discovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.AbstractDiscoveryLifecycle;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.cloud.consul.client.AgentClient;
import org.springframework.cloud.consul.model.Service;

/**
 * @author Spencer Gibb
 */
@Slf4j
public class ConsulLifecycle extends AbstractDiscoveryLifecycle {

    @Autowired
    private AgentClient agentClient;

    @Autowired
    private ConsulProperties consulProperties;

    @Override
    protected void register() {
        Service service = new Service();
        String appName = getAppName();
        service.setId(getContext().getId());
        service.setName(appName);
        //TODO: support port = 0 random assignment
        Integer port = new Integer(getEnvironment().getProperty("server.port", "8080"));
        service.setPort(port);
        service.setTags(consulProperties.getTags());

        //TODO: add support for Check
        register(service);
    }

    @Override
    protected void registerManagement() {
        Service management = new Service();
        management.setId(getManagementServiceId());
        management.setName(getManagementServiceName());
        management.setPort(getManagementPort());
        management.setTags(consulProperties.getManagementTags());

        register(management);
    }

    protected void register(Service service) {
        log.info("Registering service with consul: {}", service.toString());
        agentClient.register(service);
    }

    @Override
    protected Object getConfiguration() {
        return consulProperties;
    }

    @Override
    protected void deregister(){
        deregister(getContext().getId());
    }

    @Override
    protected void deregisterManagement() {
        deregister(getManagementServiceName());
    }

    private void deregister(String serviceId) {
        agentClient.deregister(serviceId);
    }

    @Override
    protected boolean isEnabled() {
        return consulProperties.isEnabled();
    }
}
