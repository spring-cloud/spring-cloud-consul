package org.springframework.cloud.consul.discovery;

import com.netflix.loadbalancer.Server;
import org.springframework.cloud.consul.model.ServiceNode;

/**
 * @author Spencer Gibb
 */
public class ConsulServer extends Server {

    private final MetaInfo metaInfo;
    private final String address;

    public ConsulServer(final ServiceNode node) {
        super(node.getNode(), node.getServicePort());
        address = node.getAddress();
        metaInfo = new MetaInfo() {
            @Override
            public String getAppName() {
                return node.getServiceName();
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
                return node.getServiceID();
            }
        };
    }

    @Override
    public MetaInfo getMetaInfo() {
        return metaInfo;
    }

    public String getAddress() {
        return address;
    }
}
