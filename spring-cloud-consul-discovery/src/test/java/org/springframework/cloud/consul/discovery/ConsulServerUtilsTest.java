package org.springframework.cloud.consul.discovery;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Semenkov Alexey
 */
public class ConsulServerUtilsTest {

	@Test
	public void testAddressFormat() {
		String s1 = ConsulServerUtils.fixIPv6Address("fc00:ec:cd::242:ac11:c");
		assertEquals("[fc00:ec:cd:0:0:242:ac11:c]", s1);

		String s2 = ConsulServerUtils.fixIPv6Address("[fc00:ec:cd::242:ac11:c]");
		assertEquals("[fc00:ec:cd:0:0:242:ac11:c]", s2);

		String s3 = ConsulServerUtils.fixIPv6Address("192.168.0.1");
		assertEquals("192.168.0.1", s3);

		String s4 = ConsulServerUtils.fixIPv6Address("projects.spring.io");
		assertEquals("projects.spring.io", s4);

		String s5 = ConsulServerUtils.fixIPv6Address("veryLongHostName");
		assertEquals("veryLongHostName", s5);

	}
}
