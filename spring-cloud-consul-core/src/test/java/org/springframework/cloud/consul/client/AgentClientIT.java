package org.springframework.cloud.consul.client;

import com.google.common.collect.Lists;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.consul.model.Check;
import org.springframework.cloud.consul.model.Service;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Spencer Gibb
 * Date: 4/18/14
 * Time: 11:04 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringApplicationConfiguration(classes = TestClientConfiguration.class)
public class AgentClientIT {

    @Autowired
    AgentClient client;

    @Test
    public void test001RegisterService() {
        Service service = new Service();
        service.setId("test1id");
        service.setName("test1Name");
        service.setPort(9999);
        service.setTags(Lists.newArrayList("test1tag1", "test1tag2"));
        Check check = new Check();
        check.setScript("/usr/local/bin/gtrue");
        check.setInterval(60);
        service.setCheck(check);
        client.register(service);
    }

    @Test
    public void test002GetServices() {
        Map<String, Service> services = client.getServices();
        assertNotNull("services was null", services);
        assertFalse("services was empty", services.isEmpty());
    }

    @Test
    public void test003DeregisterService() {
        client.deregister("test1id");
    }

    @Test
    public void test004GetSelf() {
        Map<String, Object> self = client.getSelf();
        assertNotNull("self was null", self);
    }
}
