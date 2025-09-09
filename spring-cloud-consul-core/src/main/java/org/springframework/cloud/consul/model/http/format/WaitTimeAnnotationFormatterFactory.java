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

package org.springframework.cloud.consul.model.http.format;

import java.util.Set;

import org.springframework.format.AnnotationFormatterFactory;
import org.springframework.format.Formatter;
import org.springframework.format.Parser;
import org.springframework.format.Printer;

public class WaitTimeAnnotationFormatterFactory implements AnnotationFormatterFactory<WaitTimeFormat> {

	private static final Set<Class<?>> FIELD_TYPES = Set.of(Long.class);

	@Override
	public Set<Class<?>> getFieldTypes() {
		return FIELD_TYPES;
	}

	@Override
	public Printer<?> getPrinter(WaitTimeFormat annotation, Class<?> fieldType) {
		return configureFormatterFrom(annotation, fieldType);
	}

	@Override
	public Parser<?> getParser(WaitTimeFormat annotation, Class<?> fieldType) {
		return configureFormatterFrom(annotation, fieldType);
	}

	private Formatter<Long> configureFormatterFrom(WaitTimeFormat annotation, Class<?> fieldType) {
		return new WaitTimeFormatter();
	}

}
