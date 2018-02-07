package org.springframework.cloud.consul.config;

import com.ecwid.consul.v1.ConsulClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.consul.ConsulProperties;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class ConsulPropertyPrefixTests {

	private ConsulClient client;

	@Before
	public void setup() {
		ConsulProperties properties = new ConsulProperties();
		client = new ConsulClient(properties.getHost(), properties.getPort());
	}

	@After
	public void teardown() {
		client.deleteKVValues("");
	}

	@Test
	public void testEmptyPrefix() {
		// because prefix is empty, a leading forward slash is omitted
		String kvContext = "appname";
		client.setKVValue(kvContext + "/fooprop", "fookvval");
		client.setKVValue(kvContext + "/bar/prop", "8080");

		ConsulPropertySource source = getConsulPropertySource(new ConsulConfigProperties(), kvContext);
		assertProperties(source, "fookvval", "8080");
	}

	private void assertProperties(ConsulPropertySource source, Object fooval, Object barval) {
		assertThat("fooprop was wrong", source.getProperty("fooprop"), is(equalTo(fooval)));
		assertThat("bar.prop was wrong", source.getProperty("bar.prop"), is(equalTo(barval)));
	}

	@SuppressWarnings("Duplicates")
	private ConsulPropertySource getConsulPropertySource(ConsulConfigProperties configProperties, String context) {
		ConsulPropertySource source = new ConsulPropertySource(context, client, configProperties);
		source.init();
		String[] names = source.getPropertyNames();
		assertThat("names was null", names, is(notNullValue()));
		assertThat("names was wrong size", names.length, is(equalTo(2)));
		return source;
	}
}
