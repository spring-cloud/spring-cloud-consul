/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.consul.discovery.filters;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.cloud.consul.discovery.ConsulServiceInstance;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

/**
 * This class filters services based on the existence of certain tags. If a service has
 * any tag that matches the set of excludeded tags, the service will be rejected by this
 * filter. If a service has no excluded tags and has a tag that matches the set of
 * included tags, the service will be accepted by this filter. the
 * {@linkplain #TagMatchingDiscoveryFilter(Set) one-argument constructor} of this class
 * includes all tags by default. To only include services with a certain tag, use the
 * {@linkplain #TagMatchingDiscoveryFilter(Set, Set) two argument constructor} with an
 * empty {@link Set} for the {@code excludeTagSet} parameter.
 *
 * @author Adam Hawthorne
 */
public class TagMatchingDiscoveryFilter implements ConsulServiceDiscoveryFilter {

	/** Default includeTagSet */
	public static final TagSet MATCH_ALL = new TagSet() {
		@Override
		public boolean contains(final String tag) {
			return true;
		}
	};

	/** Default excludeTagSet */
	public static final TagSet MATCH_NONE = new TagSet() {
		@Override
		public boolean contains(final String tag) {
			return false;
		}
	};

	private final TagSet excludeTagSet;
	private final TagSet includeTagSet;

	/**
	 * Constructor that will accept all services. Using this constructor indicates that no
	 * services should be filtered.
	 */
	public TagMatchingDiscoveryFilter() {
		this(MATCH_NONE, MATCH_ALL);
	}

	/**
	 * Constructor specifying only excluded tags. Using this constructor indicates the
	 * filter should include all tags.
	 *
	 * @param excludeTagSet {@link Set} of tags of services to reject from the filter. The
	 * argument is copied.
	 */
	public TagMatchingDiscoveryFilter(final Set<String> excludeTagSet) {
		this(copySet(excludeTagSet));
	}

	/**
	 * Constructor specifying only excluded tags. Using this constructor indicates the
	 * filter should include all tags.
	 *
	 * @param excludeTagSet {@link TagSet} of tags of services to reject from the filter.
	 * The argument is NOT copied.
	 */
	public TagMatchingDiscoveryFilter(final TagSet excludeTagSet) {
		this(excludeTagSet, MATCH_ALL);
	}

	/**
	 * Constructor specifying excluded and included tags. Excluded tags are applied to the
	 * entire list of tags first, then the included tags, if the service was not excluded.
	 *
	 * @param excludeTagSet {@link Set} of tags of services to reject from the filter. The
	 * argument is copied.
	 * @param includeTagSet {@link Set} of tags of services to accept from the filter (if
	 * not previously rejected by the {{@code excludeTagSet}. The argument is copied.
	 */
	public TagMatchingDiscoveryFilter(final Set<String> excludeTagSet,
			final Set<String> includeTagSet) {
		this(copySet(excludeTagSet), copySet(includeTagSet));
	}

	/**
	 * Constructor specifying excluded and included tags. Excluded tags are applied to the
	 * entire list of tags first, then the included tags, if the service was not excluded.
	 *
	 * @param excludeTagSet {@link Set} of tags of services to reject from the filter. The
	 * argument is NOT copied.
	 * @param includeTagSet {@link Set} of tags of services to accept from the filter (if
	 * not previously rejected by the {{@code excludeTagSet}. The argument is NOT copied.
	 */
	public TagMatchingDiscoveryFilter(final TagSet excludeTagSet,
			final TagSet includeTagSet) {
		this.excludeTagSet = Preconditions.checkNotNull(excludeTagSet, "excludeTagSet");
		this.includeTagSet = Preconditions.checkNotNull(includeTagSet, "includeTagSet");
	}

	/**
	 * @param instance The {@link ConsulServiceInstance} being tested.
	 * @return {@code true} if {@code instance.getTags()} contains no excluded tags and
	 * contains at least one included tag, {@code false} otherwise.
	 */
	@Override
	public boolean accept(final ConsulServiceInstance instance) {
		boolean result = false;
		// Always include the empty tag. This will ensure that the MATCH_ALL Set matches
		// a service with no tags.
		for (String tag : Iterables.concat(Arrays.asList(""), instance.getTags())) {
			if (isExcluded(tag)) {
				return false;
			}
			if (isIncluded(tag)) {
				result = true;
			}
		}
		return result;
	}

	/**
	 *
	 * @param tag the tested tag
	 * @return {@code true} if the tag is in the exclude set, {@code false} otherwise.
	 */
	public boolean isExcluded(final String tag) {
		return excludeTagSet.contains(tag);
	}

	/**
	 * @param tag the tested tag
	 * @return {@code true} if the tag is in the includeSet, {@code false} otherwise.
	 */
	public boolean isIncluded(final String tag) {
		return includeTagSet.contains(tag);
	}

	/**
	 * Convenience method to produce a {@link TagSet} from a {@link Set}.
	 *
	 * @param set the {@link Set} to wrap.
	 *
	 * @return a {@link TagSet} that wraps the provided set. Changes to the argument will
	 * be reflected in the return value.
	 */
	public static TagSet wrapSet(final Set<String> set) {
		return new WrappedSetTagSet(set);
	}

	/**
	 * Convenience method to produce a {@link TagSet} from a {@link Set}.
	 *
	 * @param set the {@link Set} to copy.
	 *
	 * @return a {@link TagSet} that copies the provided set. Changes to the argument will
	 * NOT be reflected in the return value.
	 */
	public static TagSet copySet(final Set<String> set) {
		return wrapSet(new HashSet<>(set));
	}

	/**
	 * This class exists to provide a slimmed down interface to a Set. The only required
	 * operation by this class is set membership. Defensive convenience methods are
	 * provided to interact with {@link Set}.
	 */
	public static interface TagSet {
		public boolean contains(String tag);
	}

	private static class WrappedSetTagSet implements TagSet {
		private final Set<String> wrappedSet;

		WrappedSetTagSet(final Set<String> wrappedSet) {
			this.wrappedSet = Preconditions.checkNotNull(wrappedSet, "wrappedSet");
		}

		/**
		 * @param tag tag being tested for membership
		 *
		 * @return {@code true} if the set contains the tag, {@code false} otherwise.
		 */
		@Override
		public boolean contains(final String tag) {
			return wrappedSet.contains(tag);
		}
	}
}
