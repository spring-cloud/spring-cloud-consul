package org.springframework.cloud.consul.discovery;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.Check;

import static com.ecwid.consul.v1.health.model.Check.CheckStatus.CRITICAL;
import static com.ecwid.consul.v1.health.model.Check.CheckStatus.PASSING;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Stéphane Leroy
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TtlSchedulerRemoveTests.TtlSchedulerRemoveTestConfig.class,
	properties = { "spring.application.name=ttlSchedulerRemove",
		"spring.cloud.consul.discovery.instance-id=ttlSchedulerRemove-id",
		"spring.cloud.consul.discovery.heartbeat.enabled=true",
		"spring.cloud.consul.discovery.heartbeat.ttlValue=2" },
		webEnvironment = RANDOM_PORT)
public class TtlSchedulerRemoveTests {

	@Autowired
	private ConsulClient consul;

	@Autowired
	private TtlScheduler ttlScheduler;

	@Test
	public void should_not_send_check_if_service_removed() throws InterruptedException {
		Thread.sleep(1000); // wait for Ttlscheduler to send a check to consul.
		Check serviceCheck = getCheckForService("ttlSchedulerRemove");
		assertThat("Service check is in wrong state", serviceCheck.getStatus(),
				equalTo(PASSING));

		// Remove service from TtlScheduler and wait for TTL to expired.
		ttlScheduler.remove("ttlSchedulerRemove-id");
		Thread.sleep(2100);
		serviceCheck = getCheckForService("ttlSchedulerRemove");
		assertThat("Service check is in wrong state", serviceCheck.getStatus(),
				equalTo(CRITICAL));
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
	@EnableAutoConfiguration
	@Import({ AutoServiceRegistrationConfiguration.class,
			ConsulAutoConfiguration.class,
			ConsulDiscoveryClientConfiguration.class })
	public static class TtlSchedulerRemoveTestConfig { }
}

