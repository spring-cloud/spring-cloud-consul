package org.springframework.cloud.consul.client;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.consul.model.Check;
import org.springframework.cloud.consul.model.Service;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author Spencer Gibb
 * Date: 4/18/14
 * Time: 11:04 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringApplicationConfiguration(classes = TestClientConfiguration.class)
public class AgentClientIT {

    private static final String SERVICE_ID = "testId"+ UUID.randomUUID().toString();
    private static final String SERVICE_NAME = "testId"+ UUID.randomUUID().toString();

    @Autowired
    AgentClient client;

    @Test
    public void test001RegisterService() {
        Service service = new Service();
        service.setId(SERVICE_ID);
        service.setName(SERVICE_NAME);
        service.setPort(9999);
        service.setTags(Arrays.asList("test1tag1", "test1tag2"));
        Check check = new Check();
        check.setScript("/usr/local/bin/gtrue");
        check.setInterval(60 + "s");
        check.setTtl(10 + "s");
        service.setCheck(check);
        client.register(service);
    }

    @Test
    public void test002CheckIsThere() {
        client.pass(SERVICE_ID);
    }

    @Test
    public void test003GetServices() {
        Map<String, Service> services = client.getServices();
        assertNotNull("services was null", services);
        assertFalse("services was empty", services.isEmpty());
    }

    @Test
    public void test004DeregisterService() {
        client.deregister(SERVICE_ID);
    }

    @Test
    public void test005GetSelf() {
        Map<String, Object> self = client.getSelf();
        assertNotNull("self was null", self);
    }
}
