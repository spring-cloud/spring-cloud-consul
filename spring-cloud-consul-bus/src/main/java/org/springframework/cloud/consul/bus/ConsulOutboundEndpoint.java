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

package org.springframework.cloud.consul.bus;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.event.model.Event;
import com.ecwid.consul.v1.event.model.EventParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.messaging.Message;

/**
 * Adapter that converts and sends Messages as Consul events
 * @author Spencer Gibb
 */
public class ConsulOutboundEndpoint extends AbstractReplyProducingMessageHandler {

    @Autowired
    protected ConsulClient consul;

    @Override
    protected Object handleRequestMessage(Message<?> requestMessage) {
        Object payload = requestMessage.getPayload();
        //TODO: support headers
        //TODO: support consul event filters: NodeFilter, ServiceFilter, TagFilter
        Response<Event> event = consul.eventFire("springCloudBus", (String) payload, new EventParams(), QueryParams.DEFAULT);
        //TODO: return event?
        return null;
    }
}
