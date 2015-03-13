package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by nicu on 11.03.2015.
 */
@Slf4j
public class TtlScheduler {

    public static final DateTime EXPIRED_DATE = new DateTime(0);
    private final Map<String, DateTime> serviceHeartbeats = new ConcurrentHashMap<>();

    private HeartbeatProperties configuration;

    private ConsulClient client;

    public TtlScheduler(HeartbeatProperties configuration, ConsulClient client) {
        this.configuration = configuration;
        this.client = client;
    }

    /**
     * Add a service to the checks loop.
     */
    public void add(final NewService service) {
        serviceHeartbeats.put(service.getId(), EXPIRED_DATE);
    }

    public void remove(String serviceId) {
        serviceHeartbeats.remove(serviceId);
    }

    @Scheduled(initialDelay = 0, fixedRateString = "${consul.heartbeat.fixedRate:15000}")
    private void heartbeatServices() {
        for (String serviceId : serviceHeartbeats.keySet()) {
            DateTime latestHeartbeatDoneForService = serviceHeartbeats.get(serviceId);
            if (latestHeartbeatDoneForService.plus(configuration.getHeartbeatInterval())
                    .isBefore(DateTime.now())) {
                String checkId = serviceId;
                if (!checkId.startsWith("service:")) {
                    checkId = "service:"+checkId;
                }
                client.agentCheckPass(checkId);
                log.info("Sending consul heartbeat for: "+serviceId);
                serviceHeartbeats.put(serviceId, DateTime.now());
            }
        }
    }
}