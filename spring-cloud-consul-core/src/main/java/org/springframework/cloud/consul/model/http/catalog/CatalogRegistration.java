/*
 * Copyright 2026-present the original author or authors.
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

package org.springframework.cloud.consul.model.http.catalog;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.core.style.ToStringCreator;

/**
 * This class is for /v1/catalog/register API.
 *
 * @author Varnson Fan
 * @since 5.0.3
 */
public class CatalogRegistration {

	@JsonProperty("Datacenter")
	private String datacenter;

	@JsonProperty("ID")
	private String id;

	@JsonProperty("Node")
	private String node;

	@JsonProperty("Address")
	private String address;

	@JsonProperty("TaggedAddresses")
	private Map<String, String> taggedAddresses;

	@JsonProperty("NodeMeta")
	private Map<String, String> nodeMeta;

	@JsonProperty("Service")
	private Service service;

	@JsonProperty("Check")
	private Check check;

	@JsonProperty("SkipNodeUpdate")
	private Boolean skipNodeUpdate;

	public String getDatacenter() {
		return datacenter;
	}

	public void setDatacenter(String datacenter) {
		this.datacenter = datacenter;
	}

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

	public Map<String, String> getTaggedAddresses() {
		return taggedAddresses;
	}

	public void setTaggedAddresses(Map<String, String> taggedAddresses) {
		this.taggedAddresses = taggedAddresses;
	}

	public Map<String, String> getNodeMeta() {
		return nodeMeta;
	}

	public void setNodeMeta(Map<String, String> nodeMeta) {
		this.nodeMeta = nodeMeta;
	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public Check getCheck() {
		return check;
	}

	public void setCheck(Check check) {
		this.check = check;
	}

	public Boolean getSkipNodeUpdate() {
		return skipNodeUpdate;
	}

	public void setSkipNodeUpdate(Boolean skipNodeUpdate) {
		this.skipNodeUpdate = skipNodeUpdate;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("datacenter", datacenter)
			.append("id", id)
			.append("node", node)
			.append("address", address)
			.append("taggedAddresses", taggedAddresses)
			.append("nodeMeta", nodeMeta)
			.append("service", service)
			.append("check", check)
			.append("skipNodeUpdate", skipNodeUpdate)
			.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CatalogRegistration that = (CatalogRegistration) o;
		return Objects.equals(datacenter, that.datacenter) && Objects.equals(id, that.id)
				&& Objects.equals(node, that.node) && Objects.equals(address, that.address)
				&& Objects.equals(taggedAddresses, that.taggedAddresses) && Objects.equals(nodeMeta, that.nodeMeta)
				&& Objects.equals(service, that.service) && Objects.equals(check, that.check)
				&& Objects.equals(skipNodeUpdate, that.skipNodeUpdate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(datacenter, id, node, address, taggedAddresses, nodeMeta, service, check, skipNodeUpdate);
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

		@JsonProperty("TaggedAddresses")
		private Map<String, TaggedServiceAddress> taggedAddresses;

		@JsonProperty("Meta")
		private Map<String, String> meta;

		@JsonProperty("Port")
		private Integer port;

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

		public Map<String, TaggedServiceAddress> getTaggedAddresses() {
			return taggedAddresses;
		}

		public void setTaggedAddresses(Map<String, TaggedServiceAddress> taggedAddresses) {
			this.taggedAddresses = taggedAddresses;
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

		@Override
		public String toString() {
			return new ToStringCreator(this).append("id", id)
				.append("service", service)
				.append("tags", tags)
				.append("address", address)
				.append("taggedAddresses", taggedAddresses)
				.append("meta", meta)
				.append("port", port)
				.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Service that = (Service) o;
			return Objects.equals(id, that.id) && Objects.equals(service, that.service)
					&& Objects.equals(tags, that.tags) && Objects.equals(address, that.address)
					&& Objects.equals(taggedAddresses, that.taggedAddresses) && Objects.equals(meta, that.meta)
					&& Objects.equals(port, that.port);
		}

		@Override
		public int hashCode() {
			return Objects.hash(id, service, tags, address, taggedAddresses, meta, port);
		}

	}

	public static class TaggedServiceAddress {

		@JsonProperty("Address")
		private String address;

		@JsonProperty("Port")
		private Integer port;

		public String getAddress() {
			return address;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public Integer getPort() {
			return port;
		}

		public void setPort(Integer port) {
			this.port = port;
		}

		@Override
		public String toString() {
			return new ToStringCreator(this).append("address", address).append("port", port).toString();
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			TaggedServiceAddress that = (TaggedServiceAddress) o;
			return Objects.equals(address, that.address) && Objects.equals(port, that.port);
		}

		@Override
		public int hashCode() {
			return Objects.hash(address, port);
		}

	}

	public static class Check {

		@JsonProperty("Node")
		private String node;

		@JsonProperty("CheckID")
		private String checkId;

		@JsonProperty("Name")
		private String name;

		@JsonProperty("Notes")
		private String notes;

		@JsonProperty("Status")
		private String status;

		@JsonProperty("ServiceID")
		private String serviceId;

		@JsonProperty("Definition")
		private CheckDefinition definition;

		public String getNode() {
			return node;
		}

		public void setNode(String node) {
			this.node = node;
		}

		public String getCheckId() {
			return checkId;
		}

		public void setCheckId(String checkId) {
			this.checkId = checkId;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getNotes() {
			return notes;
		}

		public void setNotes(String notes) {
			this.notes = notes;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getServiceId() {
			return serviceId;
		}

		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}

		public CheckDefinition getDefinition() {
			return definition;
		}

		public void setDefinition(CheckDefinition definition) {
			this.definition = definition;
		}

		@Override
		public String toString() {
			return new ToStringCreator(this).append("node", node)
				.append("checkId", checkId)
				.append("name", name)
				.append("notes", notes)
				.append("status", status)
				.append("serviceId", serviceId)
				.append("definition", definition)
				.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			Check that = (Check) o;
			return Objects.equals(node, that.node) && Objects.equals(checkId, that.checkId)
					&& Objects.equals(name, that.name) && Objects.equals(notes, that.notes)
					&& Objects.equals(status, that.status) && Objects.equals(serviceId, that.serviceId)
					&& Objects.equals(definition, that.definition);
		}

		@Override
		public int hashCode() {
			return Objects.hash(node, checkId, name, notes, status, serviceId, definition);
		}

	}

	public static class CheckDefinition {

		@JsonProperty("HTTP")
		private String http;

		@JsonProperty("TCP")
		private String tcp;

		@JsonProperty("Interval")
		private String interval;

		@JsonProperty("Timeout")
		private String timeout;

		@JsonProperty("DeregisterCriticalServiceAfter")
		private String deregisterCriticalServiceAfter;

		public String getHttp() {
			return http;
		}

		public void setHttp(String http) {
			this.http = http;
		}

		public String getTcp() {
			return tcp;
		}

		public void setTcp(String tcp) {
			this.tcp = tcp;
		}

		public String getInterval() {
			return interval;
		}

		public void setInterval(String interval) {
			this.interval = interval;
		}

		public String getTimeout() {
			return timeout;
		}

		public void setTimeout(String timeout) {
			this.timeout = timeout;
		}

		public String getDeregisterCriticalServiceAfter() {
			return deregisterCriticalServiceAfter;
		}

		public void setDeregisterCriticalServiceAfter(String deregisterCriticalServiceAfter) {
			this.deregisterCriticalServiceAfter = deregisterCriticalServiceAfter;
		}

		@Override
		public String toString() {
			return new ToStringCreator(this).append("tcp", tcp)
				.append("http", http)
				.append("interval", interval)
				.append("timeout", timeout)
				.append("deregisterCriticalServiceAfter", deregisterCriticalServiceAfter)
				.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || getClass() != o.getClass()) {
				return false;
			}
			CheckDefinition that = (CheckDefinition) o;
			return Objects.equals(tcp, that.tcp) && Objects.equals(interval, that.interval)
					&& Objects.equals(timeout, that.timeout)
					&& Objects.equals(deregisterCriticalServiceAfter, that.deregisterCriticalServiceAfter);
		}

		@Override
		public int hashCode() {
			return Objects.hash(tcp, interval, timeout, deregisterCriticalServiceAfter);
		}

	}

}
