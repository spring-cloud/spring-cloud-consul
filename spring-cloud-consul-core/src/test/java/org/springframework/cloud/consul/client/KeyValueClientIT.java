package org.springframework.cloud.consul.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.consul.model.KeyValue;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestClientConfiguration.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class KeyValueClientIT {

    public static final String KEY = "test/testkey";
    public static final String VALUE = "TestPut." + System.currentTimeMillis();

    @Autowired
    KeyValueClient keyValueClient;

    @Autowired(required = false)
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void test001Put() {

        boolean actual = keyValueClient.put(KEY, VALUE);
        assertTrue("Invalid resposne", actual);
    }

    @Test
    public void test002Get() throws IOException {
        List<KeyValue> values = keyValueClient.getKeyValue(KEY);
        assertNotNull("values is null", values);
        assertFalse("Values is null", values.isEmpty());
        assertTrue("Values is not size 1", values.size() == 1);
        KeyValue keyValue = values.get(0);
        //TODO: how to deal with this?
        String decoded = objectMapper.readValue(keyValue.getDecoded(), String.class);

        assertEquals(decoded, VALUE);
    }

    @Test
    public void test003GetKeyRecurse() {
        List<KeyValue> values = keyValueClient.getKeyValueRecurse(KEY);
        assertNotNull("values is null", values);
        assertFalse("Values is null", values.isEmpty());
    }

    @Test
    public void test004GetRecurse() {
        List<KeyValue> values = keyValueClient.getKeyValueRecurse();
        assertNotNull("values is null", values);
        assertFalse("Values is null", values.isEmpty());
    }

    @Test
    public void test005Delete() {
        keyValueClient.delete(KEY);
    }

    @Test(expected = NotFoundException.class)
    public void test006KeyNotFound() {
        keyValueClient.getKeyValue(System.currentTimeMillis()+"a123");
    }
}