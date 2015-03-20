/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.consul.discovery;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.*;

import org.joda.time.Period;
import org.junit.Test;

/**
 * @author Spencer Gibb
 */
public class HeartbeatPropertiesTests {

    @Test
    public void computeHeartbeatIntervalWorks() {
        HeartbeatProperties properties = new HeartbeatProperties();
        Period period = properties.computeHearbeatInterval();

        assertThat(period, is(notNullValue()));
        assertThat(period.getSeconds(), is(20));
    }

    @Test
    public void computeShortHeartbeat() {
        HeartbeatProperties properties = new HeartbeatProperties();
        properties.setTtlValue(2);
        Period period = properties.computeHearbeatInterval();

        assertThat(period, is(notNullValue()));
        assertThat(period.getSeconds(), is(1));
    }


}
