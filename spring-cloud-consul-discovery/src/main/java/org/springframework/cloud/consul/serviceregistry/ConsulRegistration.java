/*
 * Copyright 2013-2016 the original author or authors.
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

package org.springframework.cloud.consul.serviceregistry;

import org.springframework.cloud.client.serviceregistry.Registration;

import com.ecwid.consul.v1.agent.model.NewService;

/**
 * @author Spencer Gibb
 */
public class ConsulRegistration implements Registration {

	private final NewService service;

	public ConsulRegistration(NewService service) {
		this.service = service;
	}

	public NewService getService() {
		return service;
	}

	public String getInstanceId() {
		return getService().getId();
	}

	public String getServiceId() {
		return getService().getName();
	}

}
