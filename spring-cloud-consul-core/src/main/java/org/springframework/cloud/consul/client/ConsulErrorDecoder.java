package org.springframework.cloud.consul.client;

import feign.Response;
import feign.codec.ErrorDecoder;

/**
 * @author Spencer Gibb
 */
public class ConsulErrorDecoder extends ErrorDecoder.Default {
    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            throw new NotFoundException(response);
        }
        return super.decode(methodKey, response);
    }
}
