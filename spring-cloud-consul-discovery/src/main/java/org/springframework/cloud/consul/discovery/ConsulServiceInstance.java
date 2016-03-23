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
package org.springframework.cloud.consul.discovery;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cloud.client.DefaultServiceInstance;

/**
 * Represents a consul service, including its tags.
 *
 * @author Adam Hawthorne
 */
public class ConsulServiceInstance extends DefaultServiceInstance {

    private final List<String> tags;

    public ConsulServiceInstance(final String serviceId, final String host,
            final int port, final boolean secure, final List<String> tags) {
        super(serviceId, host, port, secure);
        this.tags = new ArrayList<>(tags);
    }

    public List<String> getTags() {
        return new ArrayList<>(tags);
    }

}
