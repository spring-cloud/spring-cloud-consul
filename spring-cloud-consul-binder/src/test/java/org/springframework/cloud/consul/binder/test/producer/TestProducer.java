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

package org.springframework.cloud.consul.binder.test.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.consul.binder.ConsulBinder;
import org.springframework.cloud.consul.binder.ConsulBinderTests;
import org.springframework.cloud.consul.binder.config.ConsulBinderConfiguration;
import org.springframework.cloud.stream.binder.PartitionSelectorStrategy;
import org.springframework.cloud.stream.binder.ProducerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.messaging.Message;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Producer application that binds a channel to a {@link ConsulBinder}
 * and sends a test message.
 */
@RestController
@Import(ConsulBinderConfiguration.class)
@Configuration
@EnableAutoConfiguration
public class TestProducer implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(TestProducer.class);

	@Autowired
	private ConsulBinder binder;

	public static void main(String[] args) {
		SpringApplication.run(TestProducer.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		/*if (args.containsOption("partitioned")
				&& Boolean.valueOf(args.getOptionValues("partitioned").get(0))) {
			binder.setPartitionSelector(stubPartitionSelectorStrategy());
		}*/
		SubscribableChannel producerChannel = producerChannel();
		ProducerProperties properties = new ProducerProperties();
		properties.setPartitionKeyExpression(new SpelExpressionParser().parseExpression("payload"));
		binder.bindProducer(ConsulBinderTests.BINDING_NAME, producerChannel, properties);

		Message<String> message = new GenericMessage<>(ConsulBinderTests.MESSAGE_PAYLOAD);
		logger.info("Writing message to binder {}", binder);
		producerChannel.send(message);
	}

	@Bean
	public SubscribableChannel producerChannel() {
		return new ExecutorSubscribableChannel();
	}

	@Bean
	public StubPartitionSelectorStrategy stubPartitionSelectorStrategy() {
		return new StubPartitionSelectorStrategy();
	}

	@RequestMapping("/partition-strategy-invoked")
	public boolean partitionStrategyInvoked() {
		return stubPartitionSelectorStrategy().invoked;
	}


	public static class StubPartitionSelectorStrategy implements PartitionSelectorStrategy {
		public volatile boolean invoked = false;

		@Override
		public int selectPartition(Object key, int partitionCount) {
			logger.info("Selecting partition for key {}; partition count: {}", key, partitionCount);
			invoked = true;
			return 1;
		}
	}

}
