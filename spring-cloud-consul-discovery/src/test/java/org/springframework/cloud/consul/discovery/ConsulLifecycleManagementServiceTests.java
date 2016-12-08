package org.springframework.cloud.consul.discovery;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @deprecated remove in Edgware
 */
@Deprecated
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestConfig.class, properties =
		{"spring.application.name=myTestService-E",
				"spring.cloud.consul.discovery.instanceId=myTestService1-E",
				"spring.cloud.consul.discovery.registerHealthCheck=false",
				"management.port=0"},
		webEnvironment = RANDOM_PORT)
public class ConsulLifecycleManagementServiceTests {

	@Autowired
	private ConsulClient consul;

	@Autowired
	private ConsulDiscoveryProperties discoveryProperties;

	@Test
	public void contextLoads() {
		Response<Map<String, Service>> response = consul.getAgentServices();
		Map<String, Service> services = response.getValue();
		Service service = services.get("myTestService-E-0-management");
		assertNotNull("service was null", service);
		assertEquals("service port is not 0", 0, service.getPort().intValue());
		assertEquals("service id was wrong", "myTestService-E-0-management", service.getId());
		assertEquals("service name was wrong", "myTestService-E-management", service.getService());
		assertFalse("service address must not be empty", StringUtils.isEmpty(service.getAddress()));
		assertEquals("service address must equals hostname from discovery properties", discoveryProperties.getHostname(), service.getAddress());
	}
}
