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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.core.style.ToStringCreator;

/**
 * This class is for /v1/catalog/deregister API.
 *
 * @author Varnson Fan
 * @since 5.0.3
 */
public class CatalogDeregistration {

	@JsonProperty("Datacenter")
	private String datacenter;

	@JsonProperty("Node")
	private String node;

	@JsonProperty("ServiceID")
	private String serviceId;

	@JsonProperty("CheckID")
	private String checkId;

	public String getDatacenter() {
		return datacenter;
	}

	public void setDatacenter(String datacenter) {
		this.datacenter = datacenter;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getCheckId() {
		return checkId;
	}

	public void setCheckId(String checkId) {
		this.checkId = checkId;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("datacenter", datacenter)
			.append("node", node)
			.append("serviceId", serviceId)
			.append("checkId", checkId)
			.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		CatalogDeregistration that = (CatalogDeregistration) o;
		return Objects.equals(datacenter, that.datacenter) && Objects.equals(node, that.node)
				&& Objects.equals(serviceId, that.serviceId) && Objects.equals(checkId, that.checkId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(datacenter, node, serviceId, checkId);
	}

}
