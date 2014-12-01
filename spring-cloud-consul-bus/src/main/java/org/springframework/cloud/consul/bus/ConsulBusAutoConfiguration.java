package org.springframework.cloud.consul.bus;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cloud.bus.BusAutoConfiguration;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;
import org.springframework.cloud.consul.client.EventClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.support.json.Jackson2JsonObjectMapper;
import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Spencer Gibb
 */
@Configuration
@ConditionalOnClass(EventClient.class)
@ConditionalOnExpression("${bus.consul.enabled:true}")
@AutoConfigureAfter(BusAutoConfiguration.class)
@EnableScheduling
public class ConsulBusAutoConfiguration {
    @Autowired
    @Qualifier("cloudBusInboundChannel") MessageChannel cloudBusInboundChannel;

    @Autowired
    ObjectMapper objectMapper;

    @Bean
    public ConsulOutboundEndpoint consulOutboundEndpoint() {
        return new ConsulOutboundEndpoint();
    }

    @Bean
    public IntegrationFlow cloudBusConsulOutboundFlow(
            @Qualifier("cloudBusOutboundChannel") MessageChannel cloudBusOutboundChannel) {
        return IntegrationFlows
                .from(cloudBusOutboundChannel)
                //TODO: put the json headers as part of the message, here?
                .transform(Transformers.toJson())
                .handle(consulOutboundEndpoint())
                .get();
    }

    @Bean
    public IntegrationFlow cloudBusConsulInboundFlow() {
        return IntegrationFlows
                .from(consulInboundChannelAdapter())
                .transform(Transformers.fromJson(RemoteApplicationEvent.class, new Jackson2JsonObjectMapper(objectMapper)))
                .channel(cloudBusInboundChannel) // now set in consulInboundChannelAdapter bean
                .get();
    }

    @Bean
    public ConsulInboundChannelAdapter consulInboundChannelAdapter() {
        ConsulInboundChannelAdapter adapter = new ConsulInboundChannelAdapter();
        adapter.setOutputChannel(cloudBusInboundChannel);
        return adapter;
    }

}
