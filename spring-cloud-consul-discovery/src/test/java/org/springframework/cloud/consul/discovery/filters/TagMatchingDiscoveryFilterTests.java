/*
 * Copyright (C) 2016 by Amobee, Inc.
 * All Rights Reserved.
 */
package org.springframework.cloud.consul.discovery.filters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.junit.Test;
import org.springframework.cloud.consul.discovery.ConsulServiceInstance;
import org.springframework.cloud.consul.discovery.filters.TagMatchingDiscoveryFilter.TagSet;

import com.google.common.collect.Sets;

/**
 *
 */
public class TagMatchingDiscoveryFilterTests {

	@Test
	public void testExcludeTags() {
		TagMatchingDiscoveryFilter filter = new TagMatchingDiscoveryFilter(
				Sets.newHashSet("exclude1", "exclude2"));
		assertTrue(filter.accept(buildInstance()));
		assertTrue(filter.accept(buildInstance("unmatched")));
		assertFalse(filter.accept(buildInstance("exclude1")));
		assertFalse(filter.accept(buildInstance("unmatched", "exclude2")));
	}

	@Test
	public void testIncludeTags() {
		Set<String> EMPTY_SET = Collections.emptySet();
		TagMatchingDiscoveryFilter filter = new TagMatchingDiscoveryFilter(EMPTY_SET,
				Sets.newHashSet("include1", "include2"));
		assertFalse(filter.accept(buildInstance()));
		assertFalse(filter.accept(buildInstance("unmatched")));
		assertTrue(filter.accept(buildInstance("include1")));
		assertTrue(filter.accept(buildInstance("unmatched", "include2")));
	}

	@Test
	public void testExcludeIncludeTags() {
		TagMatchingDiscoveryFilter filter = new TagMatchingDiscoveryFilter(
				Sets.newHashSet("exclude1", "exclude2"), Sets.newHashSet("include1", "include2"));
		assertFalse(filter.accept(buildInstance()));
		assertFalse(filter.accept(buildInstance("unmatched")));
		assertTrue(filter.accept(buildInstance("include1")));
		assertTrue(filter.accept(buildInstance("unmatched", "include1")));
		assertFalse(filter.accept(buildInstance("exclude1", "include2")));
	}

	@Test
	public void testWrappedSet() {
		Set<String> wrapped = Sets.newHashSet("a", "b");
		TagSet wrapping = TagMatchingDiscoveryFilter.wrapSet(wrapped);
		assertTrue(wrapping.contains("a"));
		assertTrue(wrapping.contains("b"));
		assertFalse(wrapping.contains("c"));

		wrapped.add("c");

		assertTrue(wrapping.contains("c"));
	}

	@Test
	public void testCopiedSet() {
		Set<String> copied = Sets.newHashSet("a", "b");
		TagSet copying = TagMatchingDiscoveryFilter.copySet(copied);
		assertTrue(copying.contains("a"));
		assertTrue(copying.contains("b"));
		assertFalse(copying.contains("c"));

		copied.add("c");

		assertFalse(copying.contains("c"));
	}

	@Test
	public void testSetConstructor() {
		Set<String> excludes = Sets.newHashSet("a");
		TagMatchingDiscoveryFilter filter = new TagMatchingDiscoveryFilter(excludes);
		assertTrue(filter.isExcluded("a"));
		assertFalse(filter.isExcluded("b"));

		excludes.add("b");
		assertFalse(filter.isExcluded("b"));
	}

	@Test
	public void testSetSetConstructor() {
		Set<String> excludes = Sets.newHashSet("a");
		Set<String> includes = Sets.newHashSet("x");
		TagMatchingDiscoveryFilter filter = new TagMatchingDiscoveryFilter(excludes, includes);
		assertTrue(filter.isExcluded("a"));
		assertFalse(filter.isExcluded("b"));

		excludes.add("b");
		assertFalse(filter.isExcluded("b"));

		assertTrue(filter.isIncluded("x"));
		assertFalse(filter.isIncluded("y"));

		includes.add("y");
		assertFalse(filter.isIncluded("y"));
	}

	@Test
	public void testDefaultConstructor() {
		TagMatchingDiscoveryFilter filter = new TagMatchingDiscoveryFilter();
		assertFalse(filter.isExcluded("a"));
		assertTrue(filter.isIncluded("a"));
	}


	private ConsulServiceInstance buildInstance(final String... tags) {
		return new ConsulServiceInstance("a", "127.0.0.1", 80, true, Arrays.asList(tags));
	}

}
