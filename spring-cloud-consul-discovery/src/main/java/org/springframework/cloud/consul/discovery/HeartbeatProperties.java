package org.springframework.cloud.consul.discovery;

import javax.annotation.PostConstruct;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Data;

import org.joda.time.Period;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "consul.heartbeat")
@Data
public class HeartbeatProperties {
    @Min(1)
    private int ttlValue = 30;

    @NotNull
    private String ttlUnit = "s";

    @DecimalMin("0.1")
    @DecimalMax("0.9")
    private double intervalRatio = 2.0 / 3.0;

    private Period heartbeatInterval;

    @PostConstruct
    public void computeHeartbeatInterval() {
        // heartbeat rate at ratio * ttl, but no later than ttl -1s and, (under lesser
        // priority), no sooner than 1s from now
        heartbeatInterval = new Period(Math.round(1000 * Math.max(ttlValue - 1,
                Math.min(ttlValue * intervalRatio, 1))));
    }

    public String getTtl() {
        return ttlValue + ttlUnit;
    }
}
