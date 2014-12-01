package org.springframework.cloud.consul.client;

import feign.Response;
import lombok.Data;

/**
 * @author Spencer Gibb
 */
@Data
public class NotFoundException extends RuntimeException {
    private final Response response;
}
