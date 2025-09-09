/*
 * Copyright 2013-present the original author or authors.
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

package org.springframework.cloud.consul.bootstraptests;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author Spencer Gibb
 */
@Configuration
@EnableAutoConfiguration
@RestController
@EnableConfigurationProperties
@EnableFeignClients
public class ConsulBootstrapApplication {

	@Autowired
	private LoadBalancerClient loadBalancer;

	@Autowired
	private DiscoveryClient discoveryClient;

	@Autowired
	private Environment env;

	@Autowired
	private SampleClient sampleClient;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private Registration registration;

	@Value("${spring.application.name:testConsulApp}")
	private String appName;

	public static void main(String[] args) {
		SpringApplication.run(ConsulBootstrapApplication.class, args);
	}

	@GetMapping("/me")
	public ServiceInstance me() {
		return this.registration;
	}

	@GetMapping("/")
	public ServiceInstance lb() {
		return this.loadBalancer.choose(this.appName);
	}

	@GetMapping("/rest")
	public String rest() {
		return this.restTemplate.getForObject("http://" + this.appName + "/me", String.class);
	}

	@GetMapping("/choose")
	public String choose() {
		return this.loadBalancer.choose(this.appName).getUri().toString();
	}

	@GetMapping("/myenv")
	public String env(@RequestParam("prop") String prop) {
		return this.env.getProperty(prop, "Not Found");
	}

	@GetMapping("/prop")
	public String prop() {
		return sampleProperties().getProp();
	}

	@GetMapping("/instances")
	public List<ServiceInstance> instances() {
		return this.discoveryClient.getInstances(this.appName);
	}

	/*
	 * @Bean public SubtypeModule sampleSubtypeModule() { return new
	 * SubtypeModule(SimpleRemoteEvent.class); }
	 */

	@GetMapping("/feign")
	public String feign() {
		return this.sampleClient.choose();
	}

	@Bean
	public SampleProperties sampleProperties() {
		return new SampleProperties();
	}

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	/*
	 * @Override public void onApplicationEvent(SimpleRemoteEvent event) {
	 * log.info("Received event: {}", event); }
	 */

	@FeignClient("testConsulApp")
	public interface SampleClient {

		@GetMapping("/choose")
		String choose();

	}

}
