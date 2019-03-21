/*
 * Copyright 2013-2019 the original author or authors.
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

package org.springframework.cloud.consul.model;

/**
 * Gossip pool (serf) statuses. Created by nicu on 10.03.2015.
 * @author Nicu Marasoiu
 */
public enum SerfStatusEnum {

	/**
	 * Alive status.
	 */
	StatusAlive(1),

	/**
	 * Leaving status.
	 */
	StatusLeaving(2),

	/**
	 * Left status.
	 */
	StatusLeft(3),

	/**
	 * Failed status.
	 */
	StatusFailed(4);

	private final int code;

	SerfStatusEnum(int code) {
		this.code = code;
	}

	public int getCode() {
		return this.code;
	}

}
