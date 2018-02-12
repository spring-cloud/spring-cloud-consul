package org.springframework.cloud.consul.discovery;

import java.util.List;
import java.util.Map;

public interface ConsulDiscoveryFilter {

    Map<String, List<String>> filter(Map<String, List<String>> services);
}
