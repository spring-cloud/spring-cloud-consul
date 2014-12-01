package org.springframework.cloud.consul.bus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.consul.client.EventService;
import org.springframework.cloud.consul.model.Event;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.messaging.Message;

/**
 * Adapter that converts and sends Messages as Consul events
 * @author Spencer Gibb
 */
public class ConsulOutboundEndpoint extends AbstractReplyProducingMessageHandler {

    @Autowired
    protected EventService eventService;

    @Override
    protected Object handleRequestMessage(Message<?> requestMessage) {
        Object payload = requestMessage.getPayload();
        //TODO: support headers
        //TODO: support consul event filters: NodeFilter, ServiceFilter, TagFilter
        Event event = eventService.fire("springCloudBus", (String) payload);
        //TODO: return event?
        return null;
    }
}
