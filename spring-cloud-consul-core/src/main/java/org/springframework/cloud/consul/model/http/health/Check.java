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

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.core.style.ToStringCreator;

public class Check {

	@JsonProperty("Node")
	private String node;

	@JsonProperty("CheckID")
	private String checkId;

	@JsonProperty("Name")
	private String name;

	@JsonProperty("Status")
	private CheckStatus status;

	@JsonProperty("Notes")
	private String notes;

	@JsonProperty("Output")
	private String output;

	@JsonProperty("ServiceID")
	private String serviceId;

	@JsonProperty("ServiceName")
	private String serviceName;

	@JsonProperty("ServiceTags")
	private List<String> serviceTags;

	@JsonProperty("CreateIndex")
	private Long createIndex;

	@JsonProperty("ModifyIndex")
	private Long modifyIndex;

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

	public CheckStatus getStatus() {
		return status;
	}

	public void setStatus(CheckStatus status) {
		this.status = status;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
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
		return new ToStringCreator(this).append("node", node)
			.append("checkId", checkId)
			.append("name", name)
			.append("status", status)
			.append("notes", notes)
			.append("output", output)
			.append("serviceId", serviceId)
			.append("serviceName", serviceName)
			.append("serviceTags", serviceTags)
			.append("createIndex", createIndex)
			.append("modifyIndex", modifyIndex)
			.toString();
	}

	public enum CheckStatus {

		/**
		 * Check status unknown.
		 */
		@JsonProperty("unknown")
		UNKNOWN,

		/**
		 * Check status passing.
		 */
		@JsonProperty("passing")
		PASSING,

		/**
		 * Check status warning.
		 */
		@JsonProperty("warning")
		WARNING,

		/**
		 * Check status critical.
		 */
		@JsonProperty("critical")
		CRITICAL

	}

}
