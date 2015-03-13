package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by nicu on 11.03.2015.
 */
@Slf4j
public class TtlScheduler implements ApplicationContextAware {
    private static final int DEFAULT_TTL = 3; // must be > 1
    public static final int HEARTBEAT_INTERVAL_RATIO = 2 / 3;

    private final Map<String, Long> serviceHeartbeats = new ConcurrentHashMap<>();
    private final AtomicBoolean heartbeatingNow = new AtomicBoolean();
    private volatile int ttl;
    private volatile int heartbeatInterval;

    @Autowired
    private ConsulClient client;

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        ttl = context.getEnvironment().getProperty("consul.ttl", Integer.class, DEFAULT_TTL);
        ttl = Math.min(2, ttl);
        // heartbeat at 2/3 ttl, but no later than ttl -1s and, (under lesser priority), no sooner than 1s from now
        heartbeatInterval = Math.max(ttl - 1, Math.min(ttl * HEARTBEAT_INTERVAL_RATIO, 1));
    }

    /**
     * Add a service to the checks loop.
     */
    public void add(final NewService service) {
        serviceHeartbeats.put(service.getId(), 0L);
    }

    public void remove(String serviceId) {
        serviceHeartbeats.remove(serviceId);
    }

    public int getTTL() {
        return ttl;
    }

    @Scheduled(initialDelay = 0, fixedRate = 100)
    void heartbeatServices() {
        if (heartbeatingNow.compareAndSet(false, true)) {
            for (String serviceId : serviceHeartbeats.keySet()) {
                long latestHeartbeatDoneForService = serviceHeartbeats.get(serviceId);
                if(latestHeartbeatDoneForService + heartbeatInterval <= System.currentTimeMillis()) {
                    client.agentCheckPass(serviceId);
                    serviceHeartbeats.put(serviceId, System.currentTimeMillis());
                }
            }
        }
    }
}