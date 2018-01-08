/*
 * Copyright 2015 the original author or authors.
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

package org.springframework.cloud.consul.binder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.consul.binder.test.consumer.TestConsumer;
import org.springframework.cloud.consul.binder.test.producer.TestProducer;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.core.AppDefinition;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.cloud.deployer.spi.local.LocalAppDeployer;
import org.springframework.cloud.deployer.spi.local.LocalDeployerProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.SocketUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link org.springframework.cloud.consul.binder.ConsulBinder}.
 *
 * @author Spencer Gibb
 */
public class ConsulBinderTests {
	private static final Logger logger = LoggerFactory.getLogger(ConsulBinderTests.class);

	/**
	 * Timeout value in milliseconds for operations to complete.
	 */
	private static final long TIMEOUT = 30000;

	/**
	 * Payload of test message.
	 */
	public static final String MESSAGE_PAYLOAD = "hello world";

	/**
	 * Name of binding used for producer and consumer bindings.
	 */
	public static final String BINDING_NAME = "test";

	/**
	 * Deployer to launch producer and consumer test applications.
	 */
	private final AppDeployer deployer;

	/**
	 * Rest template for communicating with producer/consumer test applications.
	 */
	private final RestTemplate restTemplate = new RestTemplate();


	public ConsulBinderTests() {
		LocalDeployerProperties properties = new LocalDeployerProperties();
		properties.setDeleteFilesOnExit(false);
		this.deployer = new ClasspathDeployer(properties);
	}

	/**
	 * Test basic message sending functionality.
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore //FIXME: 2.0.0 need stream fix
	public void testMessageSendReceive() throws Exception {
		testMessageSendReceive(null);
	}

	/**
	 * Test usage of partition selector.
	 *
	 * @throws Exception
	 */
	/*@Test
	public void testPartitionedMessageSendReceive() throws Exception {
		testMessageSendReceive(null, true);
	}*/

	/**
	 * Test consumer group functionality.
	 *
	 * @throws Exception
	 */
	/*@Test
	public void testMessageSendReceiveConsumerGroups() throws Exception {
		testMessageSendReceive(new String[]{"a", "b"}, false);
	}*/

	/**
	 * Test message sending functionality.
	 *
	 * @param groups consumer groups; may be {@code null}
	 * @param partitioned if true, execute test with a partition selector
	 * @throws Exception
	 */
	private void testMessageSendReceive(String[] groups) throws Exception {
		Set<AppId> consumers = null;
		AppId producer = null;

		try {
			consumers = launchConsumers(groups);
			producer = launchProducer();

			for (AppId consumer : consumers) {
				assertEquals(MESSAGE_PAYLOAD, waitForMessage(consumer.port));
			}
		}
		finally {
			if (producer != null) {
				shutdownApplication(producer.id);
			}
			if (consumers != null) {
				for (AppId consumer : consumers) {
					shutdownApplication(consumer.id);
				}
			}
		}
	}

	/**
	 * Launch one or more consumers based on the number of consumer groups.
	 * Blocks execution until the consumers are bound.
	 *
	 * @param groups consumer groups; may be {@code null}
	 * @return a set of {@link AppId}s for the consumers
	 * @throws InterruptedException
	 */
	private Set<AppId> launchConsumers(String[] groups) throws InterruptedException {
		Set<AppId> consumers = new HashSet<>();

		Map<String, String> appProperties = new HashMap<>();
		int consumerCount = groups == null ? 1 : groups.length;
		for (int i = 0; i < consumerCount; i++) {
			int consumerPort = SocketUtils.findAvailableTcpPort();
			appProperties.put("server.port", String.valueOf(consumerPort));
			List<String> args = new ArrayList<>();
			args.add(String.format("--server.port=%d", consumerPort));
			args.add("--management.context-path=/");
			args.add("--management.security.enabled=false");
			args.add("--endpoints.shutdown.enabled=true");
			args.add("--debug");
			if (groups != null) {
				args.add(String.format("--group=%s", groups[i]));
			}
			consumers.add(new AppId(launchApplication(TestConsumer.class, appProperties, args), consumerPort));
		}
		for (AppId app : consumers) {
			waitForConsumer(app.port);
		}

		return consumers;
	}

	/**
	 * Launch a producer that publishes a test message.
	 *
	 * @return {@link AppId} for producer
	 */
	private AppId launchProducer() {
		int producerPort = SocketUtils.findAvailableTcpPort();
		Map<String, String> appProperties = new HashMap<>();
		appProperties.put("server.port", String.valueOf(producerPort));
		List<String> args = new ArrayList<>();
		args.add(String.format("--server.port=%d", producerPort));
		args.add("--management.context-path=/");
		args.add("--management.security.enabled=false");
		args.add("--endpoints.shutdown.enabled=true");
		args.add(String.format("--partitioned=%b", false));
		args.add("--debug");

		return new AppId(launchApplication(TestProducer.class, appProperties, args), producerPort);
	}

	/**
	 * Block the executing thread until the consumer is bound.
	 *
	 * @param port server port of the consumer application
	 * @throws InterruptedException if the thread is interrupted
	 * @throws AssertionError if the consumer is not bound after
	 * {@value #TIMEOUT} milliseconds
	 */
	private void waitForConsumer(int port) throws InterruptedException {
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() < start + TIMEOUT) {
			if (isConsumerBound(port)) {
				return;
			}
			else {
				Thread.sleep(1000);
			}
		}
		assertTrue("Consumer not bound", isConsumerBound(port));
	}

	/**
	 * Return {@code true} if the consumer at the provided port is bound.
	 *
	 * @param port http port for consumer
	 * @return true if consumer is bound
	 */
	private boolean isConsumerBound(int port) {
		try {
			return restTemplate.getForObject(
					String.format("http://localhost:%d/is-bound", port), Boolean.class);
		}
		catch (ResourceAccessException e) {
			logger.trace("isConsumerBound", e);
			return false;
		}
	}

	/**
	 * Return the most recent payload message a consumer received.
	 *
	 * @param port http port for consumer
	 * @return the most recent payload message a consumer received;
	 *         may be {@code null}
	 */
	private String getConsumerMessagePayload(int port) {
		try {
			return restTemplate.getForObject(
					String.format("http://localhost:%d/message-payload", port), String.class);
		}
		catch (ResourceAccessException e) {
			logger.debug("getConsumerMessagePayload", e);
			return null;
		}
	}

	/**
	 * Return {@code true} if the producer made use of a custom partition selector.
	 *
	 * @param port http port for producer
	 * @return true if the producer used a custom partition selector
	 */
	private boolean partitionSelectorUsed(int port) throws InterruptedException {
		try {
			return restTemplate.getForObject(
					String.format("http://localhost:%d/partition-strategy-invoked", port),
					Boolean.class);
		}
		catch (ResourceAccessException e) {
			logger.debug("partitionSelectorUsed", e);
			return false;
		}
	}

	/**
	 * Block the executing thread until a message is received by the
	 * consumer application, or until {@value #TIMEOUT} milliseconds elapses.
	 *
	 * @param port server port of the consumer application
	 * @return the message payload that was received
	 * @throws InterruptedException if the thread is interrupted
	 */
	private String waitForMessage(int port) throws InterruptedException {
		long start = System.currentTimeMillis();
		String message = null;
		while (System.currentTimeMillis() < start + TIMEOUT) {
			message = getConsumerMessagePayload(port);
			if (message == null) {
				Thread.sleep(1000);
			}
			else {
				break;
			}
		}
		return message;
	}

	/**
	 * Launch an application in a separate JVM.
	 *
	 * @param clz the main class to launch
	 * @param properties the properties to pass to the application
	 * @param args the command line arguments for the application
	 * @return a string identifier for the application
	 */
	private String launchApplication(Class<?> clz, Map<String, String> properties, List<String> args) {
		Resource resource = new UrlResource(clz.getProtectionDomain().getCodeSource().getLocation());

		properties.put(AppDeployer.GROUP_PROPERTY_KEY, "test-group");
		properties.put("main", clz.getName());
		properties.put("classpath", System.getProperty("java.class.path"));

		String appName = String.format("%s-%s", clz.getSimpleName(), properties.get("server.port"));
		AppDefinition definition = new AppDefinition(appName, properties);

		AppDeploymentRequest request = new AppDeploymentRequest(definition, resource, properties, args);
		return this.deployer.deploy(request);
	}

	/**
	 * Shut down the application with the provided id.
	 *
	 * @param id id of application to shut down
	 */
	private void shutdownApplication(String id) {
		this.deployer.undeploy(id);
	}

	private static class ClasspathDeployer extends LocalAppDeployer {

		/**
		 * Instantiates a new local app deployer.
		 *
		 * @param properties the properties
		 */
		ClasspathDeployer(LocalDeployerProperties properties) {
			super(properties);
		}

		/**
		 * Builds the jar execution command.
		 *
		 * @param jarPath the jar path
		 * @param request the request
		 * @return the string[]
		 */
		protected String[] buildJarExecutionCommand(String jarPath, AppDeploymentRequest request) {

			ArrayList<String> commands = new ArrayList<>();
			commands.add(super.getLocalDeployerProperties().getJavaCmd());
			commands.add("-cp");
			commands.add(request.getDefinition().getProperties().get("classpath"));
			commands.add(request.getDefinition().getProperties().get("main"));
			commands.addAll(request.getCommandlineArguments());

			return commands.toArray(new String[commands.size()]);
		}
	}


	/**
	 * String identification and http port for a launched application.
	 */
	private static class AppId {
		final String id;
		final int port;

		AppId(String id, int port) {
			this.id = id;
			this.port = port;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			AppId appId = (AppId) o;
			return port == appId.port && id.equals(appId.id);
		}

		@Override
		public int hashCode() {
			int result = id.hashCode();
			result = 31 * result + port;
			return result;
		}
	}

}
