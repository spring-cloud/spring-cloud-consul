package org.springframework.cloud.consul.discovery;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import lombok.extern.slf4j.Slf4j;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;

/**
 * Created by nicu on 11.03.2015.
 */
@Slf4j
@ConfigurationProperties(prefix = "consul.heartbeat")
public class TtlScheduler {

	public static final DateTime EXPIRED_DATE = new DateTime(0);
	private final Map<String, DateTime> serviceHeartbeats = new ConcurrentHashMap<>();
	private final AtomicBoolean heartbeatingNow = new AtomicBoolean();

    @Min(1)
    @Max(10)
	private volatile int ttl = 3;

    @DecimalMin("0.1")
    @DecimalMax("0.9")
	private volatile double intervalRatio = 2.0/3.0;

	private volatile Period heartbeatInterval;

	@Autowired
	private ConsulClient client;

	@PostConstruct
	public void computeHeartbeatInterval() {
		// heartbeat rate at ratio * ttl, but no later than ttl -1s and, (under lesser
		// priority), no sooner than 1s from now
		heartbeatInterval = new Period(Math.round(1000 * Math.max(ttl - 1,
				Math.min(ttl * intervalRatio, 1))));
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

	public int getTTL() {
		return ttl;
	}

	@Scheduled(initialDelay = 0, fixedRateString = "${consul.heartbeat.fixedRate:201000}")
	private void heartbeatServices() {
		if (heartbeatingNow.compareAndSet(false, true)) {
			for (String serviceId : serviceHeartbeats.keySet()) {
				DateTime latestHeartbeatDoneForService = serviceHeartbeats.get(serviceId);
				if (latestHeartbeatDoneForService.plus(heartbeatInterval).isBefore(
						new DateTime())) {
					client.agentCheckPass(serviceId);
					serviceHeartbeats.put(serviceId, new DateTime());
				}
			}
		}
	}
}