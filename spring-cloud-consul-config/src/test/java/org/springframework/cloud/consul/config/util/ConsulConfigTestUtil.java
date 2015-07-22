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

import java.util.Set;

import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

/**
 * 
 * @author Andrew DePompa
 *
 */
public class ConsulConfigTestUtil {
	public static final String DEFAULT_FAIL_MESSAGE = "Listener never captured event";

	public static String failMessage;

	public static final String TEST_CHANGE_VALUE = "config/application/testChangeValue";

	public static final String TEST_ADD_VALUE = "config/application/testAddValue";

	public static final String TEST_DELETE_VALUE = "config/application/testDeleteValue";

	public static String expectedValue;

	public static boolean testing = false;

	@Bean
	public EnvironmentChangeEventHandler envChangeEventHandler() {
		return new EnvironmentChangeEventHandler();
	}

	public static class EnvironmentChangeEventHandler implements ApplicationListener<EnvironmentChangeEvent> {
		@Override
		public void onApplicationEvent(EnvironmentChangeEvent event) {
			if (testing) {
				System.out.println("Handling EnvironmentChangeEvent");
				Set<String> keys = event.getKeys();
				if (keys.size() != 1) {
					failMessage = "EnvironmentChangeEvent should have 1 key but had: " + keys.size();
				} else if (!keys.contains(expectedValue)) {
					failMessage = "Event does not contain key = " + expectedValue;
				} else {
					failMessage = null;
				}
			}
		}
	}
}
