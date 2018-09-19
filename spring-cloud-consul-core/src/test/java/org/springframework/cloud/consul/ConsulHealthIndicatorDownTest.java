package org.springframework.cloud.consul;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Lomesh Patel (lomeshpatel)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(properties = "spring.cloud.consul.host=invalidhost")
public class ConsulHealthIndicatorDownTest {

	@Autowired
	private HealthEndpoint healthEndpoint;

	@Test
	public void doHealthCheck() {
		assertEquals("health status was not DOWN", Status.DOWN,
				healthEndpoint.health().getStatus());
	}

	@EnableAutoConfiguration
	@SpringBootConfiguration
	protected static class TestConfig {
	}
}