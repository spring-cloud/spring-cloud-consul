/*
 * Copyright 2013-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.consul.model.http.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.core.style.ToStringCreator;

public class Event {

	@JsonProperty("ID")
	private String id;

	@JsonProperty("Name")
	private String name;

	@JsonProperty("Payload")
	private String payload;

	@JsonProperty("NodeFilter")
	private String nodeFilter;

	@JsonProperty("ServiceFilter")
	private String serviceFilter;

	@JsonProperty("TagFilter")
	private String tagFilter;

	@JsonProperty("Version")
	private int version;

	@JsonProperty("LTime")
	private int lTime;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public String getNodeFilter() {
		return nodeFilter;
	}

	public void setNodeFilter(String nodeFilter) {
		this.nodeFilter = nodeFilter;
	}

	public String getServiceFilter() {
		return serviceFilter;
	}

	public void setServiceFilter(String serviceFilter) {
		this.serviceFilter = serviceFilter;
	}

	public String getTagFilter() {
		return tagFilter;
	}

	public void setTagFilter(String tagFilter) {
		this.tagFilter = tagFilter;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getlTime() {
		return lTime;
	}

	public void setlTime(int lTime) {
		this.lTime = lTime;
	}

	/**
	 * Converted from
	 * https://github.com/hashicorp/consul/blob/master/api/event.go#L90-L104 This is a
	 * hack. It simulates the index generation to convert an event ID into a WaitIndex.
	 * @return a Wait Index value suitable for passing in to
	 * {@link org.springframework.cloud.consul.ConsulClient.QueryParams} for blocking
	 * eventList calls.
	 */
	public long getWaitIndex() {
		if (id == null || id.length() != 36) {
			return 0;
		}
		long lower = 0;
		long upper = 0;
		for (int i = 0; i < 18; i++) {
			if (i != 8 && i != 13) {
				lower = lower * 16 + Character.digit(id.charAt(i), 16);
			}
		}
		for (int i = 19; i < 36; i++) {
			if (i != 23) {
				upper = upper * 16 + Character.digit(id.charAt(i), 16);
			}
		}
		return lower ^ upper;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("id", id)
			.append("name", name)
			.append("payload", payload)
			.append("nodeFilter", nodeFilter)
			.append("serviceFilter", serviceFilter)
			.append("tagFilter", tagFilter)
			.append("version", version)
			.append("lTime", lTime)
			.toString();
	}

}
