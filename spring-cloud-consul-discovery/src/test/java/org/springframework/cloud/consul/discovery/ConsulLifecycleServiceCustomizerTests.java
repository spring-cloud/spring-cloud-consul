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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.context.ApplicationContext;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;

/**
 * 
 * @author Venil Noronha
 */
public class ConsulLifecycleServiceCustomizerTests {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void setNullServiceCustomizersThrows() {
		ConsulLifecycle lifecycle = getLifecycle();
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("ConsulServiceCustomizers must not be null");
		lifecycle.setServiceCustomizers(null);
	}

	@Test
	public void addNullServiceCustomizersThrows() {
		ConsulLifecycle lifecycle = getLifecycle();
		this.thrown.expect(IllegalArgumentException.class);
		this.thrown.expectMessage("ConsulServiceCustomizers must not be null");
		lifecycle.addServiceCustomizers((ConsulServiceCustomizer[]) null);
	}

	@Test
	public void serviceCustomizations() throws Exception {
		ConsulLifecycle lifecycle = getLifecycle();
		ConsulServiceCustomizer[] customizers = new ConsulServiceCustomizer[4];
		for (int i = 0; i < customizers.length; i++) {
			customizers[i] = mock(ConsulServiceCustomizer.class);
		}
		lifecycle.setServiceCustomizers(Arrays.asList(customizers[0], customizers[1]));
		lifecycle.addServiceCustomizers(customizers[2], customizers[3]);
		InOrder ordered = inOrder((Object[]) customizers);
		lifecycle.start();
		for (ConsulServiceCustomizer customizer : customizers) {
			ordered.verify(customizer).customize((NewService) anyObject());
		}
	}

	private ConsulLifecycle getLifecycle() {
		ConsulClient client = new ConsulClient();
		InetUtils inetUtils = new InetUtils(new InetUtilsProperties());
		ConsulDiscoveryProperties discoveryProperties = new ConsulDiscoveryProperties(inetUtils);
		discoveryProperties.setServiceName("testService");
		discoveryProperties.setInstanceId("testInstance");
		HeartbeatProperties heartbeatProperties = mock(HeartbeatProperties.class);
		ConsulLifecycle lifecycle = new ConsulLifecycle(client, discoveryProperties, heartbeatProperties);
		ApplicationContext applicationContext = mock(ApplicationContext.class);
		NoSuchBeanDefinitionException beanException = new NoSuchBeanDefinitionException("Bean not found");
		when(applicationContext.getBean(ManagementServerProperties.class)).thenThrow(beanException);
		lifecycle.setApplicationContext(applicationContext);
		lifecycle.setConfiguredPort(8000);
		ConsulLifecycle lifecycleSpy = spy(lifecycle);
		doAnswer(new Answer<Void>() {
			@Override public Void answer(InvocationOnMock invocation) throws Throwable {
				return null;
			}
		}).when(lifecycleSpy).register((NewService) any());
		return lifecycleSpy;
	}

}
