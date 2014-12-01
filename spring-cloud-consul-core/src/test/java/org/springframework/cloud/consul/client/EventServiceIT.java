package org.springframework.cloud.consul.client;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.consul.model.Event;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Spencer Gibb
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestClientConfiguration.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EventServiceIT {

    @Autowired
    private EventService service;

    @Test
    public void test001InitialIndex() {
        assertNotNull("initialIndex was null", service.getLastIndex());
    }

    @Test
    public void test002ToIndex() {
        String eventId = "bf24ae36-d240-9666-7343-1a87346d2f94";
        BigInteger index = service.toIndex(eventId);
        assertEquals("wrong index generated", new BigInteger("14728939782502463986"), index);
    }

    @Test
    public void test003GetEvents() {
        Event event = service.fire("testEvent", "test003GetEvents" + System.currentTimeMillis());
        assertNotNull("event was null", event);

        List<Event> events = service.getEvents(service.getLastIndex());
        assertNotNull("events was null", events);
        assertFalse("events was empty", events.isEmpty());
    }
}
