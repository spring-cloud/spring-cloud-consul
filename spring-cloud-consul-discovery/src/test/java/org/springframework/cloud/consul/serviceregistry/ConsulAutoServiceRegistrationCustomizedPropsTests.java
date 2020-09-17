/*
 * Copyright 2013-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.consul.serviceregistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.health.HealthChecksForServiceRequest;
import com.ecwid.consul.v1.health.model.Check;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.consul.ConsulAutoConfiguration;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.test.ConsulTestcontainers;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Spencer Gibb
 * @author Venil Noronha
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ConsulAutoServiceRegistrationCustomizedPropsTests.TestPropsConfig.class, properties = {
		"spring.application.name=myTestService-B", "spring.cloud.consul.discovery.instanceId=myTestService1-B",
		"spring.cloud.consul.discovery.port=4452", "spring.cloud.consul.discovery.hostname=myhost",
		"spring.cloud.consul.discovery.ipAddress=10.0.0.1", "spring.cloud.consul.discovery.registerHealthCheck=false",
		"spring.cloud.consul.discovery.failFast=false",
		"spring.cloud.consul.discovery.default-zone-metadata-name=mydefaultzonemetadataname",
		"spring.cloud.consul.discovery.instance-zone=myzone", "spring.cloud.consul.discovery.instance-group=mygroup",
		"spring.cloud.consul.discovery.tags[0]=mytag", "spring.cloud.consul.discovery.enableTagOverride=true",
		"spring.cloud.consul.discovery.metadata.key1=value1", "spring.cloud.consul.discovery.metadata.key2=value2" },
		webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = ConsulTestcontainers.class)
public class ConsulAutoServiceRegistrationCustomizedPropsTests {

	@Autowired
	private ConsulClient consul;

	@Autowired
	private ConsulDiscoveryProperties properties;

	@Test
	public void propertiesAreCorrect() {
		Response<Map<String, Service>> response = this.consul.getAgentServices();
		Map<String, Service> services = response.getValue();
		Service service = services.get("myTestService1-B");
		assertThat(service).as("service was null").isNotNull();
		assertThat(service.getPort()).as("service port is discovery port").isEqualTo(4452);
		assertThat("myTestService1-B").as("service id was wrong").isEqualTo(service.getId());
		assertThat("myTestService-B").as("service name was wrong").isEqualTo(service.getService());
		assertThat("myhost").as("property hostname was wrong").isEqualTo(this.properties.getHostname());
		assertThat("10.0.0.1").as("property ipAddress was wrong").isEqualTo(this.properties.getIpAddress());
		assertThat("myhost").as("service address was wrong").isEqualTo(service.getAddress());
		assertThat(service.getEnableTagOverride()).as("property enableTagOverride was wrong").isTrue();
		assertThat(service.getTags()).as("property tags contains the wrong values").containsExactly("mytag");
		HashMap<String, String> entries = new HashMap<>();
		entries.put("key1", "value1");
		entries.put("key2", "value2");
		entries.put("mydefaultzonemetadataname", "myzone");
		entries.put("group", "mygroup");
		entries.put("secure", "false");
		assertThat(service.getMeta()).as("property metadata contains the wrong entries")
				.containsExactlyInAnyOrderEntriesOf(entries);

		Response<List<Check>> checkResponse = this.consul.getHealthChecksForService("myTestService-B",
				HealthChecksForServiceRequest.newBuilder().setQueryParams(QueryParams.DEFAULT).build());
		List<Check> checks = checkResponse.getValue();
		assertThat(checks).as("checks was wrong size").hasSize(0);
	}

	@Test
	public void testFailFastDisabled() {
		assertThat(this.properties.isFailFast()).as("property failFast was wrong").isFalse();
	}

	@Configuration(proxyBeanMethods = false)
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class, ConsulAutoConfiguration.class,
			ConsulAutoServiceRegistrationAutoConfiguration.class })
	public static class TestPropsConfig {

	}

}
