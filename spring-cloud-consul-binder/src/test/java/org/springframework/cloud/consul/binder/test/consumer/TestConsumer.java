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

package org.springframework.cloud.consul.binder.test.consumer;

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
import org.springframework.cloud.stream.binder.ConsumerProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.ExecutorSubscribableChannel;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consumer application that binds a channel to a {@link ConsulBinder}
 * and stores the received message payload.
 */
@RestController
@Import(ConsulBinderConfiguration.class)
@Configuration
@EnableAutoConfiguration
public class TestConsumer implements ApplicationRunner {
	private static final Logger logger = LoggerFactory.getLogger(TestConsumer.class);

	/**
	 * Flag that indicates if the consumer has been bound.
	 */
	private volatile boolean isBound = false;

	/**
	 * Payload of last received message.
	 */
	private volatile String messagePayload;

	@Autowired
	private ConsulBinder binder;

	/**
	 * Main method.
	 *
	 * @param args if present, first arg is consumer group name
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		SpringApplication.run(TestConsumer.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		logger.info("Consumer running with binder {}", binder);
		SubscribableChannel consumerChannel = new ExecutorSubscribableChannel();
		consumerChannel.subscribe(new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				messagePayload = (String) message.getPayload();
				logger.info("Received message: {}", messagePayload);
			}
		});
		String group = null;

		if (args.containsOption("group")) {
			group = args.getOptionValues("group").get(0);
		}

		binder.bindConsumer(ConsulBinderTests.BINDING_NAME, group, consumerChannel,
				new ConsumerProperties());
		isBound = true;
	}

	@RequestMapping("/is-bound")
	public boolean isBound() {
		return isBound;
	}

	@RequestMapping("/message-payload")
	public String getMessagePayload() {
		return messagePayload;
	}

}
