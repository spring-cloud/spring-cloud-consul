/*
 * Copyright 2013-2020 the original author or authors.
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

package org.springframework.cloud.consul.configdatatests;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author Spencer Gibb
 */
@SpringBootApplication
@RestController
@EnableConfigurationProperties
@Slf4j
public class ConsulConfigDataApplication {

	@Autowired
	private LoadBalancerClient loadBalancer;

	@Autowired
	private DiscoveryClient discoveryClient;

	@Autowired
	private Environment env;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private Registration registration;

	@Value("${spring.application.name:testConsulApp}")
	private String appName;

	public static void main(String[] args) {
		SpringApplication.run(ConsulConfigDataApplication.class, args);
	}

	@RequestMapping("/me")
	public ServiceInstance me() {
		return this.registration;
	}

	@RequestMapping("/")
	public ServiceInstance lb() {
		return this.loadBalancer.choose(this.appName);
	}

	@RequestMapping("/rest")
	public String rest() {
		return this.restTemplate.getForObject("http://" + this.appName + "/me", String.class);
	}

	@RequestMapping("/choose")
	public String choose() {
		return this.loadBalancer.choose(this.appName).getUri().toString();
	}

	@RequestMapping("/myenv")
	public String env(@RequestParam("prop") String prop) {
		return this.env.getProperty(prop, "Not Found");
	}

	@RequestMapping("/prop")
	public String prop() {
		return sampleProperties().getProp();
	}

	@RequestMapping("/instances")
	public List<ServiceInstance> instances() {
		return this.discoveryClient.getInstances(this.appName);
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

}
