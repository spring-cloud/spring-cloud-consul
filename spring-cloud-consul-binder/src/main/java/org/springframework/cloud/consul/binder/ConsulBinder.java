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

package org.springframework.cloud.consul.binder;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.cloud.stream.binder.AbstractBinder;
import org.springframework.cloud.stream.binder.Binding;
import org.springframework.cloud.stream.binder.DefaultBinding;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.util.Assert;

/**
 * @author Spencer Gibb
 */
public class ConsulBinder
		extends AbstractBinder<MessageChannel, ExtendedConsumerProperties<ConsulConsumerProperties>, ExtendedProducerProperties<ConsulProducerProperties>>
		implements ExtendedPropertiesBinder<MessageChannel, ConsulConsumerProperties, ConsulProducerProperties> {

	private static final String BEAN_NAME_TEMPLATE = "outbound.%s";

	private ConsulExtendedBindingProperties consulExtendedBindingProperties;

	private final ConsulClient consulClient;

	public ConsulBinder(ConsulClient consulClient) {
		this.consulClient = consulClient;
	}

	public void setConsulExtendedBindingProperties(ConsulExtendedBindingProperties consulExtendedBindingProperties) {
		this.consulExtendedBindingProperties = consulExtendedBindingProperties;
	}

	@Override
	public ConsulConsumerProperties getExtendedConsumerProperties(String channelName) {
		return consulExtendedBindingProperties.getExtendedConsumerProperties(channelName);
	}

	@Override
	public ConsulProducerProperties getExtendedProducerProperties(String channelName) {
		return consulExtendedBindingProperties.getExtendedProducerProperties(channelName);
	}

	@Override
	protected Binding<MessageChannel> doBindConsumer(String name, String group, MessageChannel inputTarget, ExtendedConsumerProperties<ConsulConsumerProperties> properties) {
		return null;
	}

	@Override
	protected Binding<MessageChannel> doBindProducer(String name, MessageChannel channel, ExtendedProducerProperties<ConsulProducerProperties> properties) {
		Assert.isInstanceOf(SubscribableChannel.class, channel);

		logger.debug("Binding Consul client to eventName " + name);
		ConsulSendingHandler sendingHandler = new ConsulSendingHandler(this.consulClient, name);

		EventDrivenConsumer consumer = new EventDrivenConsumer((SubscribableChannel) channel, sendingHandler);
		consumer.setBeanFactory(getBeanFactory());
		consumer.setBeanName(String.format(BEAN_NAME_TEMPLATE, name));
		consumer.afterPropertiesSet();
		consumer.start();

		return new DefaultBinding<>(name, null, channel, consumer);
	}
}
