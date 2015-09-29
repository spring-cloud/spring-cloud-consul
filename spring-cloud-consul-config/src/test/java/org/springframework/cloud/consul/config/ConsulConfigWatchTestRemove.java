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
package org.springframework.cloud.consul.config;

import static org.springframework.cloud.consul.config.util.ConsulConfigTestUtil.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ecwid.consul.v1.ConsulClient;

import lombok.extern.slf4j.Slf4j;

/**
 * Separated the remove test from the others as the keys conflicted since the environment does not actually update in these tests
 * 
 * @author Andrew DePompa
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ConsulConfigWatchTestRemove.MyTestConfig.class,
		org.springframework.cloud.consul.config.util.ConsulConfigTestUtil.EnvironmentChangeEventHandler.class })
@WebIntegrationTest(value = { "spring.application.name=testConsulConfigWatchRemove",
		"spring.cloud.consul.config.watch=true" }, randomPort = true)
@Slf4j
public class ConsulConfigWatchTestRemove {
	@Autowired
	private ConsulClient client;

	@Before
	public void setup() {
		failMessage = DEFAULT_FAIL_MESSAGE;
		client.setKVValue(TEST_DELETE_VALUE, "default value");

		testing = true;
	}

	@After
	public void cleanup() {
		testing = false;
		client.deleteKVValue(TEST_DELETE_VALUE);
	}

	private void deleteValue(String value) throws InterruptedException {
		expectedValue = value;
		Thread.sleep(2000);
		log.info("Changing value...");
		client.deleteKVValue(value);
		Thread.sleep(2000);

		if (failMessage != null) {
			Assert.fail(failMessage);
		}
	}

	@Test
	public void removeValueTest() throws InterruptedException {
		deleteValue(TEST_DELETE_VALUE);
	}

	@Configuration
	@EnableAutoConfiguration
	@ComponentScan
	public static class MyTestConfig {
		// ignore
	}
}
