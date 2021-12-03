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

package org.springframework.cloud.consul.test;

import java.util.HashMap;

import com.ecwid.consul.v1.ConsulClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

public class ConsulTestcontainers implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	static final Logger logger = LoggerFactory.getLogger(ConsulTestcontainers.class);

	/**
	 * Default consul port.
	 */
	public static final int DEFAULT_PORT = 8500;

	/**
	 * Shared consul container.
	 */
	public static GenericContainer<?> consul = createConsulContainer();

	public static GenericContainer<?> createConsulContainer() {
		return createConsulContainer("1.7");
	}

	public static GenericContainer<?> createConsulContainer(String consulVersion) {
		String dockerImageName = "consul:" + consulVersion;
		return new GenericContainer<>(dockerImageName)
				.withLogConsumer(new Slf4jLogConsumer(logger).withSeparateOutputStreams())
				.waitingFor(Wait.forHttp("/v1/status/leader")).withExposedPorts(DEFAULT_PORT)
				.withCommand("agent", "-dev", "-server", "-bootstrap", "-client", "0.0.0.0", "-log-level", "trace");
	}

	@Override
	public void initialize(ConfigurableApplicationContext context) {
		start();

		MutablePropertySources sources = context.getEnvironment().getPropertySources();

		if (!sources.contains("consulTestcontainer")) {
			Integer mappedPort = consul.getMappedPort(DEFAULT_PORT);
			HashMap<String, Object> map = new HashMap<>();
			map.put(ConsulProperties.PREFIX + ".port", String.valueOf(mappedPort));
			map.put(ConsulProperties.PREFIX + ".host", consul.getContainerIpAddress());

			sources.addFirst(new MapPropertySource("consulTestcontainer", map));
		}
	}

	public static void start() {
		consul.start();
	}

	public static Integer getPort() {
		if (!consul.isRunning()) {
			throw new IllegalStateException("consul Testcontainer is not running");
		}
		return consul.getMappedPort(8500);
	}

	public static String getHost() {
		if (!consul.isRunning()) {
			throw new IllegalStateException("consul Testcontainer is not running");
		}
		return consul.getContainerIpAddress();
	}

	public static ConsulClient client() {
		return new ConsulClient(getHost(), getPort());
	}

	public static void initializeSystemProperties() {
		start();

		System.setProperty(ConsulProperties.PREFIX + ".port", getPort().toString());
		System.setProperty(ConsulProperties.PREFIX + ".host", getHost());
	}

}
