package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.catalog.model.CatalogService;
import com.netflix.loadbalancer.Server;

/**
 * @author Spencer Gibb
 */
public class ConsulServer extends Server {

    private final MetaInfo metaInfo;

    public ConsulServer(final CatalogService service) {
        super(service.getNode(), service.getServicePort());
        metaInfo = new MetaInfo() {
            @Override
            public String getAppName() {
                return service.getServiceName();
            }

            @Override
            public String getServerGroup() {
                return null;
            }

            @Override
            public String getServiceIdForDiscovery() {
                return null;
            }

            @Override
            public String getInstanceId() {
                return service.getServiceId();
            }
        };
    }

    @Override
    public MetaInfo getMetaInfo() {
        return metaInfo;
    }
}
