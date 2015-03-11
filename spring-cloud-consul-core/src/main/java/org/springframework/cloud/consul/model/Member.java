package org.springframework.cloud.consul.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.Map;

/**
 * A serf/consul cluster member.
 * @author nicu on 10.03.2015.
 */
@Data
@EqualsAndHashCode(of = {"name", "address", "port"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Member {

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Addr")
    private String address;

    @JsonProperty("Port")
    private int port;

    @JsonProperty("Tags")
    @JsonDeserialize(as = HashMap.class, keyAs = String.class, contentAs = String.class)
    private Map<String, String> tags;

    @JsonProperty("Status")
    private int status;

    @JsonProperty("ProtocolMin")
    private int protocolMin;

    @JsonProperty("ProtocolMax")
    private int protocolMax;

    @JsonProperty("ProtocolCur")
    private int protocolCur;

    @JsonProperty("DelegateMin")
    private int delegateMin;

    @JsonProperty("DelegateMax")
    private int delegateMax;

    @JsonProperty("DelegateCur")
    private int delegateCur;

}
