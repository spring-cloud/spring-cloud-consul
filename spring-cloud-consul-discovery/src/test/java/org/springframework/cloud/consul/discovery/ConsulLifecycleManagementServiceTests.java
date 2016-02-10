package org.springframework.cloud.consul.discovery;

import java.util.Map;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Service;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StringUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * @author Aleksandr Tarasov (aatarasov)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringApplicationConfiguration(classes = TestConfig.class)
@WebIntegrationTest(value = {"spring.application.name=myTestService",
		"spring.cloud.consul.discovery.instanceId=myTestService1", "management.port=0"}, randomPort = true)
public class ConsulLifecycleManagementServiceTests {
	@Autowired
	ConsulLifecycle lifecycle;

	@Autowired
	ConsulClient consul;

	@Autowired
	ConsulDiscoveryProperties discoveryProperties;

	@Test
	public void contextLoads() {
		Response<Map<String, Service>> response = consul.getAgentServices();
		Map<String, Service> services = response.getValue();
		Service service = services.get("myTestService-0-management");
		assertNotNull("service was null", service);
		assertEquals("service port is not 0", 0, service.getPort().intValue());
		assertEquals("service id was wrong", "myTestService-0-management", service.getId());
		assertEquals("service name was wrong", "myTestService-management", service.getService());
		assertFalse("service address must not be empty", StringUtils.isEmpty(service.getAddress()));
		assertEquals("service address must equals hostname from discovery properties", discoveryProperties.getHostname(), service.getAddress());
	}

	@Configuration
	@EnableAutoConfiguration
	@Import({ConsulAutoConfiguration.class,
			ConsulDiscoveryClientConfiguration.class})
	public static class TestConfig {

	}
}
