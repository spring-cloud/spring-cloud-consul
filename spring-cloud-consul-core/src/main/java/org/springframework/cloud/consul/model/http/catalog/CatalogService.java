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

package org.springframework.cloud.consul.model.http.catalog;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.core.style.ToStringCreator;

public class CatalogService {

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

	@JsonProperty("NodeMeta")
	private Map<String, String> nodeMeta;

	@JsonProperty("ServiceID")
	private String serviceId;

	@JsonProperty("ServiceName")
	private String serviceName;

	@JsonProperty("ServiceTags")
	private List<String> serviceTags;

	@JsonProperty("ServiceAddress")
	private String serviceAddress;

	@JsonProperty("ServiceMeta")
	private Map<String, String> serviceMeta;

	@JsonProperty("ServicePort")
	private Integer servicePort;

	@JsonProperty("ServiceEnableTagOverride")
	private Boolean serviceEnableTagOverride;

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

	public Map<String, String> getNodeMeta() {
		return nodeMeta;
	}

	public void setNodeMeta(Map<String, String> nodeMeta) {
		this.nodeMeta = nodeMeta;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public List<String> getServiceTags() {
		return serviceTags;
	}

	public void setServiceTags(List<String> serviceTags) {
		this.serviceTags = serviceTags;
	}

	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

	public Map<String, String> getServiceMeta() {
		return serviceMeta;
	}

	public void setServiceMeta(Map<String, String> serviceMeta) {
		this.serviceMeta = serviceMeta;
	}

	public Integer getServicePort() {
		return servicePort;
	}

	public void setServicePort(Integer servicePort) {
		this.servicePort = servicePort;
	}

	public Boolean getServiceEnableTagOverride() {
		return serviceEnableTagOverride;
	}

	public void setServiceEnableTagOverride(Boolean serviceEnableTagOverride) {
		this.serviceEnableTagOverride = serviceEnableTagOverride;
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
			.append("nodeMeta", nodeMeta)
			.append("serviceId", serviceId)
			.append("serviceName", serviceName)
			.append("serviceTags", serviceTags)
			.append("serviceAddress", serviceAddress)
			.append("serviceMeta", serviceMeta)
			.append("servicePort", servicePort)
			.append("serviceEnableTagOverride", serviceEnableTagOverride)
			.append("createIndex", createIndex)
			.append("modifyIndex", modifyIndex)
			.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CatalogService that = (CatalogService) o;
		return Objects.equals(id, that.id) && Objects.equals(node, that.node) && Objects.equals(address, that.address)
				&& Objects.equals(datacenter, that.datacenter) && Objects.equals(taggedAddresses, that.taggedAddresses)
				&& Objects.equals(nodeMeta, that.nodeMeta) && Objects.equals(serviceId, that.serviceId)
				&& Objects.equals(serviceName, that.serviceName) && Objects.equals(serviceTags, that.serviceTags)
				&& Objects.equals(serviceAddress, that.serviceAddress) && Objects.equals(serviceMeta, that.serviceMeta)
				&& Objects.equals(servicePort, that.servicePort)
				&& Objects.equals(serviceEnableTagOverride, that.serviceEnableTagOverride)
				&& Objects.equals(createIndex, that.createIndex) && Objects.equals(modifyIndex, that.modifyIndex);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, node, address, datacenter, taggedAddresses, nodeMeta, serviceId, serviceName,
				serviceTags, serviceAddress, serviceMeta, servicePort, serviceEnableTagOverride, createIndex,
				modifyIndex);
	}

}
