package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.AbstractDiscoveryLifecycle;
import org.springframework.cloud.consul.ConsulProperties;

/**
 * @author Spencer Gibb
 */
@Slf4j
public class ConsulLifecycle extends AbstractDiscoveryLifecycle {

    @Autowired
    private ConsulClient client;

    @Autowired
    private ConsulProperties consulProperties;

    @Autowired
    private TtlScheduler ttlScheduler;

    @Override
    protected void register() {
        NewService service = new NewService();
        String appName = getAppName();
        //TODO: move id to properties with context ID as default
        service.setId(getContext().getId());
        service.setName(appName);
        //TODO: support port = 0 random assignment
        Integer port = new Integer(getEnvironment().getProperty("server.port", "8080"));
        service.setPort(port);
        service.setTags(consulProperties.getTags());
        NewService.Check check = new NewService.Check();
        check.setTtl(ttlScheduler.getTTL() + "s");
        service.setCheck(check);
        register(service);
    }

    @Override
    protected void registerManagement() {
        NewService management = new NewService();
        management.setId(getManagementServiceId());
        management.setName(getManagementServiceName());
        management.setPort(getManagementPort());
        management.setTags(consulProperties.getManagementTags());

        register(management);
    }

    protected void register(NewService service) {
        log.info("Registering service with consul: {}", service.toString());
        client.agentServiceRegister(service);
        ttlScheduler.add(service);
    }

    @Override
    protected Object getConfiguration() {
        return consulProperties;
    }

    @Override
    protected void deregister() {
        deregister(getContext().getId());
    }

    @Override
    protected void deregisterManagement() {
        deregister(getManagementServiceName());
    }

    private void deregister(String serviceId) {
        ttlScheduler.remove(serviceId);
        client.agentServiceDeregister(serviceId);
    }

    @Override
    protected boolean isEnabled() {
        return consulProperties.isEnabled();
    }
}
