package org.springframework.cloud.consul.discovery;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.Check;

import static com.ecwid.consul.v1.health.model.Check.CheckStatus.PASSING;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Stéphane Leroy
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TtlSchedulerTest.TtlSchedulerTestConfig.class,
	properties = { "spring.application.name=ttlScheduler",
		"spring.cloud.consul.discovery.instanceId=ttlScheduler-id",
		"spring.cloud.consul.discovery.heartbeat.enabled=true",
		"spring.cloud.consul.discovery.heartbeat.ttlValue=2", "management.port=0" },
		webEnvironment = RANDOM_PORT)
public class TtlSchedulerTest {

	@Autowired
	private ConsulClient consul;

	@Test
	public void should_send_a_check_before_ttl_for_all_services()
			throws InterruptedException {
		Thread.sleep(2100); // Wait for TTL to expired (TTL is set to 2 seconds)

		Check serviceCheck = getCheckForService("ttlScheduler");
		assertThat("Service check is in wrong state", serviceCheck.getStatus(),
				equalTo(PASSING));
		Check serviceManagementCheck = getCheckForService("ttlScheduler-management");
		assertThat("Service management heck in wrong state",
				serviceManagementCheck.getStatus(), equalTo(PASSING));
	}

	private Check getCheckForService(String serviceId) {
		Response<List<Check>> checkResponse = consul.getHealthChecksForService(serviceId,
				QueryParams.DEFAULT);
		if (checkResponse.getValue().size() > 0) {
			return checkResponse.getValue().get(0);
		}
		return null;
	}

	@Configuration
	@EnableDiscoveryClient(autoRegister = false) //FIXME:
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ TestConsulLifecycleConfiguration.class, ConsulAutoConfiguration.class, ConsulDiscoveryClientConfiguration.class })
	public static class TtlSchedulerTestConfig { }
}


