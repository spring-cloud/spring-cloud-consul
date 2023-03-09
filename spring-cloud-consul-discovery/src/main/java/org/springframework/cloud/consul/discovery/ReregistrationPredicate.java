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

package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.OperationException;

/**
 * Predicate on whether to re-register service.
 *
 * @author Toshiaki Maki
 */
public interface ReregistrationPredicate {

	/**
	 * test if the exception is eligible for re-registration.
	 * @param e OperationException
	 * @return if the exception is eligible for re-registration
	 */
	boolean isEligible(OperationException e);

	/**
	 * Default implementation that performs re-registration when the status code is either 404 or 500.
	 */
	ReregistrationPredicate DEFAULT = e -> (e.getStatusCode() == 404 || e.getStatusCode() == 500);

}
