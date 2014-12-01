package org.springframework.cloud.consul.client;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.consul.model.Event;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestClientConfiguration.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EventClientIT {

    public static final String NAME = "testEvent";
    public static final String PAYLOAD = "TestPayload." + System.currentTimeMillis();

    @Autowired
    private EventClient client;

    @Test
    public void test001Fire() {
        Event event = client.fire(NAME, PAYLOAD);
        assertNotNull("Event was null", event);
        assertNotNull("Event Id was null", event.getId());
        assertEquals("Event name was wrong", NAME, event.getName());
    }

    @Test
    public void test002Get() {
        List<Event> values = client.getEvents();
        assertNotNull("events is null", values);
        assertFalse("Values is empty", values.isEmpty());
        /*assertTrue("Values is not size 1", values.size() == 1);
        KeyValue keyValue = values.get(0);
        //TODO: how to deal with this?
        String decoded = objectMapper.readValue(keyValue.getDecoded(), String.class);

        assertEquals(decoded, VALUE);*/
    }
}