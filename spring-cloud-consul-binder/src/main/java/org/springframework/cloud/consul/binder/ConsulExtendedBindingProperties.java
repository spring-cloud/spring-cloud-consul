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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.stream.binder.ExtendedBindingProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties("spring.cloud.stream.consul")
public class ConsulExtendedBindingProperties implements ExtendedBindingProperties<ConsulConsumerProperties, ConsulProducerProperties> {

	private Map<String, ConsulBindingProperties> bindings = new HashMap<>();

	public Map<String, ConsulBindingProperties> getBindings() {
		return bindings;
	}

	public void setBindings(Map<String, ConsulBindingProperties> bindings) {
		this.bindings = bindings;
	}

	@Override
	public ConsulConsumerProperties getExtendedConsumerProperties(String channelName) {
		if (bindings.containsKey(channelName) && bindings.get(channelName).getConsumer() != null) {
			return bindings.get(channelName).getConsumer();
		}
		else {
			return new ConsulConsumerProperties();
		}
	}

	@Override
	public ConsulProducerProperties getExtendedProducerProperties(String channelName) {
		if (bindings.containsKey(channelName) && bindings.get(channelName).getProducer() != null) {
			return bindings.get(channelName).getProducer();
		}
		else {
			return new ConsulProducerProperties();
		}
	}
}
