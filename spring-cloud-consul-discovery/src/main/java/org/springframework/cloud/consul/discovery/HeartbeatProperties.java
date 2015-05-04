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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.Period;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@ConfigurationProperties(prefix = "consul.heartbeat")
@Data
@Slf4j
public class HeartbeatProperties {
    //ttlSeconds value in milli seconds
    @Min(10)
    @Max(60000)
    private int ttlSeconds = 30;

    @DecimalMin("0.1")
    @DecimalMax("0.9")
    private double heartbeatScheduleIntervalRatio = 2.0 / 3.0;

    private final Period heartbeatExpirePeriod;

    public HeartbeatProperties() {
        heartbeatExpirePeriod = hearbeatInterval();
        log.debug("HeartbeatProperties = {}", this);
    }

    public String getConsulTtl() {
        return ttlSeconds + "s";
    }

    protected Period hearbeatInterval() {
        // heartbeat expiration time at ratio * ttlSeconds, but no later than ttlSeconds -1s and, (under lesser
        // priority), no sooner than 1s from now
        return new Period((long)(ttlSeconds * 1000 * heartbeatScheduleIntervalRatio));
    }

}
