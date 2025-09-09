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

import java.text.ParseException;
import java.util.Locale;

import org.springframework.format.Formatter;

public class WaitTimeFormatter implements Formatter<Long> {

	@Override
	public Long parse(String text, Locale locale) throws ParseException {
		try {
			if (text.endsWith("s")) {
				return Long.parseLong(text.substring(0, text.length() - 1));
			}
			else {
				return Long.parseLong(text);
			}
		}
		catch (NumberFormatException e) {
			throw new ParseException(e.getMessage(), 0);
		}

	}

	@Override
	public String print(Long object, Locale locale) {
		return object + "s";
	}

}
