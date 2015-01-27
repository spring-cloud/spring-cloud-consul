package org.springframework.cloud.consul;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.agent.model.Self;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;

import java.util.List;
import java.util.Map;

/**
 * @author Spencer Gibb
 */
public class ConsulHealthIndicator extends AbstractHealthIndicator {

    @Autowired
    private ConsulClient consul;

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        try {
            Response<Self> self = consul.getAgentSelf();
            Response<Map<String, List<String>>> services = consul.getCatalogServices(QueryParams.DEFAULT);
            builder.up()
                    .withDetail("services", services.getValue())
                    .withDetail("agent", self.getValue());
        } catch (Exception e) {
            builder.down(e);
        }
    }
}
