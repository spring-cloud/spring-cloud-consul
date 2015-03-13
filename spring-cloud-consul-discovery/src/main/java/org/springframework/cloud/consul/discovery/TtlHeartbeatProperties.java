package org.springframework.cloud.consul.discovery;

import lombok.Data;
import org.joda.time.Period;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@ConfigurationProperties(prefix = "consul.heartbeat")
@Data
public class TtlHeartbeatProperties {
    @Min(1)
    @Max(10)
    private volatile int ttl = 3;

    @DecimalMin("0.1")
    @DecimalMax("0.9")
    private volatile double intervalRatio = 2.0 / 3.0;

    private volatile Period heartbeatInterval;

    @PostConstruct
    public void computeHeartbeatInterval() {
        // heartbeat rate at ratio * ttl, but no later than ttl -1s and, (under lesser
        // priority), no sooner than 1s from now
        heartbeatInterval = new Period(Math.round(1000 * Math.max(ttl - 1,
                Math.min(ttl * intervalRatio, 1))));
    }

    public String getTtlAsString() {
        return ttl + "s";
    }
}
