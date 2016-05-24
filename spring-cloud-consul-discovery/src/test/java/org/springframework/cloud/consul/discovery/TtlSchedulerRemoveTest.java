package org.springframework.cloud.consul.discovery;

import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.Check;

import static com.ecwid.consul.v1.health.model.Check.CheckStatus.CRITICAL;
import static com.ecwid.consul.v1.health.model.Check.CheckStatus.PASSING;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Stéphane Leroy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringApplicationConfiguration(classes = TtlSchedulerRemoveTestConfig.class)
@WebIntegrationTest(value = { "spring.application.name=ttlSchedulerRemove",
		"spring.cloud.consul.discovery.instanceId=ttlSchedulerRemove-id",
		"spring.cloud.consul.discovery.heartbeat.enabled=true",
		"spring.cloud.consul.discovery.heartbeat.ttlValue=2" }, randomPort = true)
public class TtlSchedulerRemoveTest {

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

}

@Configuration
@EnableDiscoveryClient
@EnableAutoConfiguration
@Import({ ConsulAutoConfiguration.class, ConsulDiscoveryClientConfiguration.class })
class TtlSchedulerRemoveTestConfig {

}
