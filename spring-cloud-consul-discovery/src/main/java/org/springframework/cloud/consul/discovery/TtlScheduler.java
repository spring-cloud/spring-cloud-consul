package org.springframework.cloud.consul.discovery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.consul.client.AgentClient;
import org.springframework.cloud.consul.model.Service;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nicu on 11.03.2015.
 */
@Slf4j
public class TtlScheduler implements ApplicationContextAware {
    private static final AtomicInteger _instances = new AtomicInteger();
    private static final int DEFAULT_TTL = 3; // must be > 1
    public static final int HEARTBEAT_INTERVAL_RATIO = 2 / 3;

    private final ScheduledExecutorService ttlExecutor;
    private volatile ScheduledFuture<?> scheduledFuture;
    private final Thread shutdownThread;
    private final Map<String, Service> servicesById;
    private final AtomicBoolean isShuttingDown;
    private final Runnable ttlPingThread;
    private volatile int ttl;
    private volatile int heartbeatInterval;

    @Autowired
    private AgentClient agentClient;

    public TtlScheduler() {
        if (_instances.addAndGet(1) > 1) {
            throw new IllegalStateException("Expecting this to be used in singleton mode!");
        }
        //one thread to refresh ttl for each registered app to local agent is ok
        ttlExecutor = Executors.newSingleThreadScheduledExecutor();
        shutdownThread = new Thread(new Runnable() {
            public void run() {
                log.info("Shutting down the Executor Pool for TtlScheduler");
                shutdownExecutorPool();
            }
        });
        Runtime.getRuntime().addShutdownHook(shutdownThread);
        servicesById = new ConcurrentHashMap<>();
        isShuttingDown = new AtomicBoolean();
        ttlPingThread = new TtlPingThread();
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        ttl = context.getEnvironment().getProperty("consul.ttl", Integer.class, DEFAULT_TTL);
        ttl = Math.min(2, ttl);
        // heartbeat at 2/3 ttl, but no later than ttl -1s and, (under lesser priority), no sooner than 1s from now
        heartbeatInterval = Math.max(ttl - 1, Math.min(ttl * HEARTBEAT_INTERVAL_RATIO, 1));
        scheduleTtlHeartbeat();
    }

    /**
     * Add a service to the checks loop.
     */
    public void add(final Service service) {
        servicesById.put(service.getId(), service);
    }

    public void remove(String serviceId) {
        servicesById.remove(serviceId);
    }

    public int getTTL() {
        return ttl;
    }


    private void schedule(int millis, Runnable command) {
        ttlExecutor.schedule(command, millis, TimeUnit.MILLISECONDS);
    }

    private void scheduleTtlHeartbeat() {
        scheduledFuture = ttlExecutor.scheduleAtFixedRate(
                ttlPingThread,
                0, heartbeatInterval,
                TimeUnit.SECONDS);
    }

    private void shutdownExecutorPool() {
        isShuttingDown.set(true);
        ttlExecutor.shutdown();
        try {
            Runtime.getRuntime().removeShutdownHook(shutdownThread);
        } catch (IllegalStateException ignored) {
        }
    }

    class TtlPingThread implements Runnable {
        public void run() {
            try {
                heartbeatServices();
            } catch (Throwable e) {
                log.error("Exception while trying to send heartbeat from application to consul local agent in due TTL", e);
            }
        }
    }

    void heartbeatServices() {
        for (Service service : servicesById.values()) {
            if (!isShuttingDown.get()) {
                agentClient.pass(service.getId());
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }
}