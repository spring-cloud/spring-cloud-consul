package org.springframework.cloud.consul.bus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapter that receives Messages from Consul Events, converts them into
 * Spring Integration Messages, and sends the results to a Message Channel.
 * @author Spencer Gibb
 */
public class ConsulInboundChannelAdapter extends MessageProducerSupport {
    //@Autowired
    //private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    public ConsulInboundChannelAdapter() {
    }

    //link eventService to sendMessage
        /*
        Map<String, Object> headers = headerMapper.toHeadersFromRequest(message.getMessageProperties());
        if (messageListenerContainer.getAcknowledgeMode() == AcknowledgeMode.MANUAL) {
            headers.put(AmqpHeaders.DELIVERY_TAG, message.getMessageProperties().getDeliveryTag());
            headers.put(AmqpHeaders.CHANNEL, channel);
        }
        sendMessage(AmqpInboundChannelAdapter.this.getMessageBuilderFactory().withPayload(payload).copyHeaders(headers).build());*/

        //start thread
        //make blocking calls
        //foreach event -> send message


    @Override
    protected void doStart() {
    }

    @Scheduled(fixedDelayString = "10")
    public void getEvents() throws IOException {
        /*FIXME: List<Event> events = eventService.watch();
        for (Event event : events) {
            Map<String, Object> headers = new HashMap<>();
            //headers.put(MessageHeaders.REPLY_CHANNEL, outputChannel.)
            sendMessage(getMessageBuilderFactory()
                    //TODO: deal with odd objectMapper thing
                    .withPayload(objectMapper.readValue(event.getDecoded(), String.class))
                            //TODO: support headers
                    .build());
        }*/
    }

    @Override
    protected void doStop() {
    }
}
