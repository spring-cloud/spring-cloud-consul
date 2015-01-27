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
