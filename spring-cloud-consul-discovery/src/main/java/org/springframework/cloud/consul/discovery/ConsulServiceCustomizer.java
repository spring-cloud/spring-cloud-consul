/*
 * Copyright 2016 the original author or authors.
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

package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.agent.model.NewService;

/**
 * Callback interface that can be used to customize a Consul {@link NewService}.
 * 
 * @author Venil Noronha
 */
public interface ConsulServiceCustomizer {

	/**
	 * Customize the Consul service.
	 * 
	 * @param service the {@link NewService} to customize.
	 */
	public void customize(NewService service);

}
