package org.springframework.cloud.consul.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.consul.model.ServiceNode;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Spencer Gibb
 * Date: 4/18/14
 * Time: 11:04 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestClientConfiguration.class)
public class CatalogClientIT {

    @Autowired
    CatalogClient client;

    @Test
    public void testGetServices() {
        Map<String, List<String>> services = client.getServices();
        assertNotNull("services is null", services);
        assertTrue("No consul key", services.containsKey("consul"));
    }

    @Test
    public void testGetService() {
        List<ServiceNode> serviceNodes = client.getServiceNodes("consul");
        assertNotNull("serviceNodes is null", serviceNodes);
        assertFalse("serviceNodes is empty", serviceNodes.isEmpty());

        ServiceNode node = serviceNodes.get(0);

        assertNotNull("address is null", node.getAddress());
        assertNotNull("node is null", node.getNode());
        assertNotNull("serviceId is null", node.getServiceID());
        assertNotNull("serviceName is null", node.getServiceName());
        assertTrue("servicePort is wrong", node.getServicePort() > 0);
    }
}
