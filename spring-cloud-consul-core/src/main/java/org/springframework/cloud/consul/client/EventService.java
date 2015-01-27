package org.springframework.cloud.consul.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Throwables;
import feign.Param;
import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.consul.model.Event;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Spencer Gibb
 */
public class EventService {

    @Autowired
    protected EventClient client;

    @Autowired(required = false)
    protected ObjectMapper objectMapper = new ObjectMapper();

    private AtomicReference<BigInteger> lastIndex = new AtomicReference<>();

    @PostConstruct
    public void init() {
        Response response = getEventsResponse();
        setLastIndex(response);
    }

    private void setLastIndex(Response response) {
        Collection<String> header = response.headers().get("X-Consul-Index");
        if (header != null && header.iterator().hasNext()) {
            lastIndex.set(new BigInteger(header.iterator().next()));
        }
    }

    public BigInteger getLastIndex() {
        return lastIndex.get();
    }

    public Event fire(@Param("name") String name, String payload) {
        return client.fire(name, payload);
    }

    public Response getEventsResponse() {
        return client.getEventsResponse();
    }

    public List<Event> getEvents() {
        return client.getEvents();
    }

    /**
     * from https://github.com/armon/consul-api/blob/master/event.go#L92-L104
     // IDToIndex is a bit of a hack. This simulates the index generation to
     // convert an event ID into a WaitIndex.
     func (e *Event) IDToIndex(uuid string) uint64 {
         lower := uuid[0:8] + uuid[9:13] + uuid[14:18]
         upper := uuid[19:23] + uuid[24:36]
         lowVal, err := strconv.ParseUint(lower, 16, 64)
         if err != nil {
             panic("Failed to convert " + lower)
         }
         highVal, err := strconv.ParseUint(upper, 16, 64)
         if err != nil {
             panic("Failed to convert " + upper)
         }
         return lowVal ^ highVal
         //^    bitwise XOR            integers
     }
     */
    public BigInteger toIndex(String eventId) {
        String lower = eventId.substring(0, 8) + eventId.substring(9, 13) + eventId.substring(14, 18);
        String upper = eventId.substring(19, 23) + eventId.substring(24, 36);
        BigInteger lowVal = new BigInteger(lower, 16);
        BigInteger highVal = new BigInteger(upper, 16);
        BigInteger index = lowVal.xor(highVal);
        return index;
    }

    public List<Event> getEvents(BigInteger lastIndex) {
        return filterEvents(readEvents(getEventsResponse()), lastIndex);
    }

    public List<Event> watch() {
        return watch(lastIndex.get());
    }

    public List<Event> watch(BigInteger lastIndex) {
        //TODO: parameterized or configurable watch time
        return filterEvents(readEvents(client.watch("2s", lastIndex.toString())), lastIndex);
    }

    protected List<Event> readEvents(Response response) {
        try {
            setLastIndex(response);
            return objectMapper.readValue(response.body().asInputStream(),
                    TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, Event.class));
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        return null;
    }

    /**
     * from https://github.com/hashicorp/consul/blob/master/watch/funcs.go#L169-L194
     */
    protected List<Event> filterEvents(List<Event> toFilter, BigInteger lastIndex) {
        List<Event> events = toFilter;
        if (lastIndex != null) {
            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                BigInteger eventIndex = toIndex(event.getId());
                if (eventIndex.equals(lastIndex)) {
                    events = events.subList(i + 1, events.size());
                    break;
                }
            }
        }
        return events;
    }

}
