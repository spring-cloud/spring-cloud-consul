package org.springframework.cloud.consul;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.consul.client.CatalogClient;

import java.util.List;
import java.util.Map;

/**
 * @author Spencer Gibb
 */
public class ConsulHealthIndicator extends AbstractHealthIndicator {

    @Autowired
    private CatalogClient catalogClient;

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            Map<String, List<String>> services = catalogClient.getServices();
            builder.up().withDetail("services", services);
        } catch (Exception e) {
            builder.down(e);
        }
    }
}
