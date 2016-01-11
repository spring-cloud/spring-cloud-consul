/*
 * Copyright 2013-2015 the original author or authors.
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
package org.springframework.cloud.consul.config.util;

import java.util.Map;

import org.springframework.cloud.consul.config.watch.ConsulKeyValueChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

/**
 * @author Andrew DePompa
 *
 */
public class ConsulConfigTestUtil {
	public static final String DEFAULT_FAIL_MESSAGE = "Listener never captured event";

	public static String failMessage;
	public static String expectedKey;
	public static String expectedValue;
	public static boolean testing = false;

	@Bean
	public ConsulKeyValueChangeEventHandler kvChangeEventHandler() {
		return new ConsulKeyValueChangeEventHandler();
	}

	public static class ConsulKeyValueChangeEventHandler implements ApplicationListener<ConsulKeyValueChangeEvent> {
		@Override
		public void onApplicationEvent(ConsulKeyValueChangeEvent event) {
			if (testing) {
				Map<String, String> properties = event.getProperties();
				if(properties == null){
					failMessage = "ConsulKeyValueChangeEvent properties should not be null";
				} else if (properties.size() != 1) {
					failMessage = "ConsulKeyValueChangeEvent should have 1 key but had: " + properties.size() + " = " + properties.toString();
				} else if (!properties.containsKey(expectedKey) && !properties.keySet().iterator().next().contains(expectedKey.substring(expectedKey.lastIndexOf('/')+1))) {
					failMessage = "Event does not contain key = " + expectedKey + ": actual = " + properties.toString();
				} else if(properties.get(expectedKey) != null && !properties.get(expectedKey).equals(expectedValue)) {
					failMessage = "Event does not contain value = " + expectedValue + ": actual = " + properties.get(expectedKey);
				} else {
					failMessage = null;
				}
			}
		}
	}
}
