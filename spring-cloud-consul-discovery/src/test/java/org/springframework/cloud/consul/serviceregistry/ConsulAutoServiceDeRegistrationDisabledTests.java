/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.consul.serviceregistry;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Service;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Jon Freedman
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulAutoServiceRegistrationDisabledTests.TestConfig.class,
        properties = {"spring.application.name=myTestNotDeRegisteredService",
                "spring.cloud.consul.discovery.instanceId=myTestNotDeRegisteredService-D",
                "spring.cloud.consul.discovery.deregister=false"},
        webEnvironment = RANDOM_PORT)
public class ConsulAutoServiceDeRegistrationDisabledTests {
    @Autowired
    private ConsulClient consul;

    @Autowired(required = false)
    private ConsulAutoServiceRegistration autoServiceRegistration;

    @Autowired(required = false)
    private ConsulDiscoveryProperties discoveryProperties;

    @Test
    public void contextLoads() {
        assertNotNull("ConsulAutoServiceRegistration was not created", autoServiceRegistration);
        assertNotNull("ConsulDiscoveryProperties was not created", discoveryProperties);

        checkService(true);
        autoServiceRegistration.deregister();
        checkService(true);
        discoveryProperties.setDeregister(true);
        autoServiceRegistration.deregister();
        checkService(false);
    }

    private void checkService(final boolean expected) {
        final Response<Map<String, Service>> response = consul.getAgentServices();
        final Map<String, Service> services = response.getValue();
        final Service service = services.get("myTestNotDeRegisteredService-D");
        if (expected) {
            assertNotNull("service was not registered", service);
        } else {
            assertNull("service was registered", service);
        }
    }

    @Configuration
    @EnableAutoConfiguration
    @ImportAutoConfiguration({AutoServiceRegistrationConfiguration.class, ConsulAutoConfiguration.class,
            ConsulAutoServiceRegistrationAutoConfiguration.class})
    public static class TestConfig {
    }
}
