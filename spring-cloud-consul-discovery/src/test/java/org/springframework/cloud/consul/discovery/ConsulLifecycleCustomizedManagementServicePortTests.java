package org.springframework.cloud.consul.discovery;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Aleksandr Tarasov (aatarasov)
 * @author Alex Antonov (aantonov)
 * @deprecated remove in Edgware
 */
@Deprecated
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfig.class,
	properties = {"spring.application.name=myTestService-G",
		"spring.cloud.consul.discovery.instanceId=myTestService1-G",
		"spring.cloud.consul.discovery.registerHealthCheck=false",
		"spring.cloud.consul.discovery.managementPort=4452", "management.port=0"},
		webEnvironment = RANDOM_PORT)
public class ConsulLifecycleCustomizedManagementServicePortTests {

	@Autowired
	private ConsulClient consul;

	@Autowired
	private ConsulDiscoveryProperties discoveryProperties;

	@Autowired
	private ManagementServerProperties managementServerProperties;

	@Test
	public void contextLoads() {
		Response<Map<String, Service>> response = consul.getAgentServices();
		Map<String, Service> services = response.getValue();
		Service service = services.get("myTestService-G-0-management");
		assertNotNull("service was null", service);
		assertEquals("service port is not 4452", 4452, service.getPort().intValue());
		assertEquals("management port is not 0", 0, managementServerProperties.getPort().intValue());
		assertEquals("service id was wrong", "myTestService-G-0-management", service.getId());
		assertEquals("service name was wrong", "myTestService-G-management", service.getService());
		assertFalse("service address must not be empty", StringUtils.isEmpty(service.getAddress()));
		assertEquals("service address must equals hostname from discovery properties", discoveryProperties.getHostname(), service.getAddress());
	}

}
