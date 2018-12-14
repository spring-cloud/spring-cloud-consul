/*
 * Copyright 2013-2016 the original author or authors.
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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Alexey Savchuk (devpreview)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        ConsulAutoServiceRegistrationManagementCustomizerTests.TestConfig.class,
        ConsulAutoServiceRegistrationManagementCustomizerTests.ManagementConfig.class
}, properties = {
        "spring.application.name=myTestService-SS",
        "spring.cloud.consul.discovery.registerHealthCheck=false",
        "management.server.port=4452"
}, webEnvironment = RANDOM_PORT)
public class ConsulAutoServiceRegistrationManagementCustomizerTests {

    @Autowired
    private ConsulRegistration registration;

    @Test
    public void contextLoads() {
        //
    }

    @Configuration
    public static class ManagementConfig {

        @Bean
        public ConsulManagementRegistrationCustomizer managementCustomizer() {
            return managementRegistration -> {
                //
            };
        }

        @Bean
        public ConsulManagementRegistrationCustomizer managementCustomizer2() {
            return managementRegistration -> {
                //
            };
        }

    }

    @Configuration
    @EnableAutoConfiguration
    @ImportAutoConfiguration({
            AutoServiceRegistrationConfiguration.class,
            ConsulAutoConfiguration.class,
            ConsulAutoServiceRegistrationAutoConfiguration.class
    })
    public static class TestConfig {
    }
}
