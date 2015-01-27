package org.springframework.cloud.consul.client;

import feign.Param;
import feign.RequestLine;
import feign.Response;
import org.springframework.cloud.consul.model.Event;

import java.util.List;

/**
 * @author Spencer Gibb
 */
public interface EventClient {
    //?node=, ?service=, and ?tag= ?dc=
    @RequestLine("PUT /v1/event/fire/{name}")
    Event fire(@Param("name") String name, String payload);

    //?name=
    //?wait=<interval>&index=<idx>
    @RequestLine("GET /v1/event/list")
    List<Event> getEvents();

    @RequestLine("GET /v1/event/list")
    Response getEventsResponse();

    @RequestLine("GET /v1/event/list?wait={wait}&index={index}")
    Response watch(@Param("wait") String wait, @Param("index") String index);
}
