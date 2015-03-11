package org.springframework.cloud.consul.model;

/**
 * Gossip pool (serf) statuses.
 * Created by nicu on 10.03.2015.
 */
public enum SerfStatusEnum {
    StatusAlive(1),
    StatusLeaving(2),
    StatusLeft(3),
    StatusFailed(4);
    private final int code;

    SerfStatusEnum(int code) {
        this.code=code;
    }

    public int getCode() {
        return code;
    }
    
}
