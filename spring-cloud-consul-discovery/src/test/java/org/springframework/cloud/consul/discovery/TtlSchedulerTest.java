package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.health.model.Check;
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

import java.util.List;

import static com.ecwid.consul.v1.health.model.Check.CheckStatus.CRITICAL;
import static com.ecwid.consul.v1.health.model.Check.CheckStatus.PASSING;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author Stéphane Leroy
 */
@RunWith(SpringJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringApplicationConfiguration(classes = TtlSchedulerTestConfig.class)
@WebIntegrationTest(value = {"spring.application.name=ttlScheduler",
        "spring.cloud.consul.discovery.instanceId=ttlScheduler-id",
        "spring.cloud.consul.discovery.heartbeat.enabled=true",
        "spring.cloud.consul.discovery.heartbeat.ttlValue=2",
        "management.port=0"},
        randomPort = true)
public class TtlSchedulerTest {

    @Autowired
    private ConsulClient consul;

    @Test
    public void should_send_a_check_before_ttl_for_all_services() throws InterruptedException {
        Thread.sleep(2100); // Wait for TTL to expired (TTL is set to 2 seconds)

        Check serviceCheck = getCheckForService("ttlScheduler");
        assertThat("Service check is in wrong state", serviceCheck.getStatus(), equalTo(PASSING));
        Check serviceManagementCheck = getCheckForService("ttlScheduler-management");
        assertThat("Service management heck in wrong state", serviceManagementCheck.getStatus(), equalTo(PASSING));
    }

    private Check getCheckForService(String serviceId) {
        Response<List<Check>> checkResponse = consul.getHealthChecksForService(serviceId, QueryParams.DEFAULT);
        if(checkResponse.getValue().size()>0) {
            return checkResponse.getValue().get(0);
        }
        return null;
    }

}

@Configuration
@EnableDiscoveryClient
@EnableAutoConfiguration
@Import({ ConsulAutoConfiguration.class, ConsulDiscoveryClientConfiguration.class })
class TtlSchedulerTestConfig {

}
