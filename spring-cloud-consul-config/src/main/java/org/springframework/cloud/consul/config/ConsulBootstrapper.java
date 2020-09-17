/*
 * Copyright 2015-2020 the original author or authors.
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

package org.springframework.cloud.consul.config;

import java.util.function.Function;

import com.ecwid.consul.v1.ConsulClient;

import org.springframework.boot.Bootstrapper;
import org.springframework.cloud.consul.ConsulProperties;

public abstract class ConsulBootstrapper {

	static Bootstrapper withConsulClient(Function<ConsulProperties, ConsulClient> factory) {
		return registry -> registry.register(ConsulClient.class, context -> {
			ConsulProperties properties = context.get(ConsulProperties.class);
			return factory.apply(properties);
		});
	}

}
