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
import lombok.extern.apachecommons.CommonsLog;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.math.BigDecimal;

@ConfigurationProperties(prefix = "spring.cloud.consul.discovery.heartbeat")
@Data
@CommonsLog
public class HeartbeatProperties {

    // TODO: change enabled to default to true when I stop seeing messages like
    // [WARN] agent: Check 'service:testConsulApp:xtest:8080' missed TTL, is now critical
    boolean enabled = false;

    @Min(10)
    @Max(60000)
    private int ttlSeconds = 30;

    @DecimalMin("0.1")
    @DecimalMax("0.9")
    private BigDecimal heartbeatScheduleIntervalRatio = new BigDecimal(0.67);

    private Period heartbeatExpirePeriod;

    @PostConstruct
    protected void computeScheduleInterval() {
        // heartbeat rate at ratio * ttl
        heartbeatExpirePeriod = new Period(
                Math.round(heartbeatScheduleIntervalRatio.doubleValue() * ttlSeconds)
                * 1000);
        if (log.isDebugEnabled()) {
            log.debug("HeartbeatProperties = {}" + this);
        }
    }

    public String getConsulTtl() {
        return ttlSeconds + "s";
    }

}
