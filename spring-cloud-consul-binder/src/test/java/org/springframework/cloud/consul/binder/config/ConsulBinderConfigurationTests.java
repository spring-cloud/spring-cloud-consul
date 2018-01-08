/*
 * Copyright 2013-2017 the original author or authors.
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

package org.springframework.cloud.consul.binder.config;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;

import static org.hamcrest.Matchers.containsString;

/**
 * @author Spencer Gibb
 */
public class ConsulBinderConfigurationTests {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	@Ignore //FIXME 2.0.0 need stream fix
	public void consulBinderDisabledWorks() {
		this.exception.expectMessage(containsString("no proper implementation found"));
		new SpringApplicationBuilder(Application.class)
				.properties("spring.cloud.consul.binder.enabled=false")
				.run();
	}

	@Test
	@Ignore //FIXME 2.0.0 need stream fix
	public void consulDisabledDisablesBinder() {
		this.exception.expectMessage(containsString("no proper implementation found"));
		new SpringApplicationBuilder(Application.class)
				.properties("spring.cloud.consul.enabled=false")
				.run();
	}

	interface Events {
		@Output
		MessageChannel purchases();
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableBinding(Events.class)
	public static class Application {
	}
}
