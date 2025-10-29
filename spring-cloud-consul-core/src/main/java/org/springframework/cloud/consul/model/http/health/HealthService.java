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

package org.springframework.cloud.consul.model.http.health;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.core.style.ToStringCreator;

public class HealthService {

	@JsonProperty("Node")
	private Node node;

	@JsonProperty("Service")
	private Service service;

	@JsonProperty("Checks")
	private List<Check> checks;

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public List<Check> getChecks() {
		return checks;
	}

	public void setChecks(List<Check> checks) {
		this.checks = checks;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("node", node)
			.append("service", service)
			.append("checks", checks)
			.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		HealthService that = (HealthService) o;
		return Objects.equals(node, that.node) && Objects.equals(service, that.service)
				&& Objects.equals(checks, that.checks);
	}

	@Override
	public int hashCode() {
		return Objects.hash(node, service, checks);
	}

	public static class Node {

		@JsonProperty("ID")
		private String id;

		@JsonProperty("Node")
		private String node;

		@JsonProperty("Address")
		private String address;

		@JsonProperty("Datacenter")
		private String datacenter;

		@JsonProperty("TaggedAddresses")
		private Map<String, String> taggedAddresses;

		@JsonProperty("Meta")
		private Map<String, String> meta;

		@JsonProperty("CreateIndex")
		private Long createIndex;

		@JsonProperty("ModifyIndex")
		private Long modifyIndex;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getNode() {
			return node;
		}

		public void setNode(String node) {
			this.node = node;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public String getDatacenter() {
			return datacenter;
		}

		public void setDatacenter(String datacenter) {
			this.datacenter = datacenter;
		}

		public Map<String, String> getTaggedAddresses() {
			return taggedAddresses;
		}

		public void setTaggedAddresses(Map<String, String> taggedAddresses) {
			this.taggedAddresses = taggedAddresses;
		}

		public Map<String, String> getMeta() {
			return meta;
		}

		public void setMeta(Map<String, String> meta) {
			this.meta = meta;
		}

		public Long getCreateIndex() {
			return createIndex;
		}

		public void setCreateIndex(Long createIndex) {
			this.createIndex = createIndex;
		}

		public Long getModifyIndex() {
			return modifyIndex;
		}

		public void setModifyIndex(Long modifyIndex) {
			this.modifyIndex = modifyIndex;
		}

		@Override
		public String toString() {
			return new ToStringCreator(this).append("id", id)
				.append("node", node)
				.append("address", address)
				.append("datacenter", datacenter)
				.append("taggedAddresses", taggedAddresses)
				.append("meta", meta)
				.append("createIndex", createIndex)
				.append("modifyIndex", modifyIndex)
				.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Node node1 = (Node) o;
			return Objects.equals(id, node1.id) && Objects.equals(node, node1.node)
					&& Objects.equals(address, node1.address) && Objects.equals(datacenter, node1.datacenter)
					&& Objects.equals(taggedAddresses, node1.taggedAddresses) && Objects.equals(meta, node1.meta)
					&& Objects.equals(createIndex, node1.createIndex) && Objects.equals(modifyIndex, node1.modifyIndex);
		}

		@Override
		public int hashCode() {
			return Objects.hash(id, node, address, datacenter, taggedAddresses, meta, createIndex, modifyIndex);
		}

	}

	public static class Service {

		@JsonProperty("ID")
		private String id;

		@JsonProperty("Service")
		private String service;

		@JsonProperty("Tags")
		private List<String> tags;

		@JsonProperty("Address")
		private String address;

		@JsonProperty("Meta")
		private Map<String, String> meta;

		@JsonProperty("Port")
		private Integer port;

		@JsonProperty("EnableTagOverride")
		private Boolean enableTagOverride;

		@JsonProperty("CreateIndex")
		private Long createIndex;

		@JsonProperty("ModifyIndex")
		private Long modifyIndex;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getService() {
			return service;
		}

		public void setService(String service) {
			this.service = service;
		}

		public List<String> getTags() {
			return tags;
		}

		public void setTags(List<String> tags) {
			this.tags = tags;
		}

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public Map<String, String> getMeta() {
			return meta;
		}

		public void setMeta(Map<String, String> meta) {
			this.meta = meta;
		}

		public Integer getPort() {
			return port;
		}

		public void setPort(Integer port) {
			this.port = port;
		}

		public Boolean getEnableTagOverride() {
			return enableTagOverride;
		}

		public void setEnableTagOverride(Boolean enableTagOverride) {
			this.enableTagOverride = enableTagOverride;
		}

		public Long getCreateIndex() {
			return createIndex;
		}

		public void setCreateIndex(Long createIndex) {
			this.createIndex = createIndex;
		}

		public Long getModifyIndex() {
			return modifyIndex;
		}

		public void setModifyIndex(Long modifyIndex) {
			this.modifyIndex = modifyIndex;
		}

		@Override
		public String toString() {
			return new ToStringCreator(this).append("id", id)
				.append("service", service)
				.append("tags", tags)
				.append("address", address)
				.append("meta", meta)
				.append("port", port)
				.append("enableTagOverride", enableTagOverride)
				.append("createIndex", createIndex)
				.append("modifyIndex", modifyIndex)
				.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Service service1 = (Service) o;
			return Objects.equals(id, service1.id) && Objects.equals(service, service1.service)
					&& Objects.equals(tags, service1.tags) && Objects.equals(address, service1.address)
					&& Objects.equals(meta, service1.meta) && Objects.equals(port, service1.port)
					&& Objects.equals(enableTagOverride, service1.enableTagOverride)
					&& Objects.equals(createIndex, service1.createIndex)
					&& Objects.equals(modifyIndex, service1.modifyIndex);
		}

		@Override
		public int hashCode() {
			return Objects.hash(id, service, tags, address, meta, port, enableTagOverride, createIndex, modifyIndex);
		}

	}

}
