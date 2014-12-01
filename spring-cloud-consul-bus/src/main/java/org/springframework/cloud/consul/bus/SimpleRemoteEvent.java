package org.springframework.cloud.consul.bus;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import org.springframework.cloud.bus.event.RemoteApplicationEvent;

/**
 * @author Spencer Gibb
 */
@JsonTypeName("simple")
@Data
public class SimpleRemoteEvent extends RemoteApplicationEvent {

    private String message;

    private SimpleRemoteEvent(){}

    public SimpleRemoteEvent(Object source, String originService, String destinationService, String message) {
        super(source, originService, destinationService);
        this.message = message;
    }

    public SimpleRemoteEvent(Object source, String originService, String message) {
        super(source, originService);
        this.message = message;
    }
}
