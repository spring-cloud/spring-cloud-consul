package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by nicu on 11.03.2015.
 */
@Slf4j
@ConfigurationProperties
public class TtlScheduler {

	private final Map<String, Long> serviceHeartbeats = new ConcurrentHashMap<>();
	private final AtomicBoolean heartbeatingNow = new AtomicBoolean();

    @Value("${consul.ttl:3}")
	private volatile int ttl;

    @Value("${consul.heartbeatIntervalRatio:0.66}")
    private volatile float heartbeatIntervalRatio;

	private volatile int heartbeatInterval;

	@Autowired
	private ConsulClient client;

	@PostConstruct
    public void computeHeartbeatInterval() {
        // heartbeat rate at ratio * ttl, but no later than ttl -1s and, (under lesser priority), no sooner than 1s from now
        heartbeatInterval = Math.round(Math.max(ttl - 1, Math.min(ttl * heartbeatIntervalRatio, 1)));
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
	private void heartbeatServices() {
		if (heartbeatingNow.compareAndSet(false, true)) {
			for (String serviceId : serviceHeartbeats.keySet()) {
				long latestHeartbeatDoneForService = serviceHeartbeats.get(serviceId);
				if (latestHeartbeatDoneForService + heartbeatInterval <= System
						.currentTimeMillis()) {
					client.agentCheckPass(serviceId);
					serviceHeartbeats.put(serviceId, System.currentTimeMillis());
				}
			}
		}
	}
}