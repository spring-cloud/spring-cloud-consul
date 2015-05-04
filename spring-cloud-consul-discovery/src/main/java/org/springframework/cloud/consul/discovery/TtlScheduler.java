/*
 * Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.consul.discovery;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.scheduling.TaskScheduler;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.*;

/**
 * Created by nicu on 11.03.2015.
 */
@Slf4j
public class TtlScheduler {
    private final SortedSet<ServiceHeartbeatRecord> serviceHeartbeats =
            new ConcurrentSkipListSet<ServiceHeartbeatRecord>(new Comparator<ServiceHeartbeatRecord>() {
                @Override
                public int compare(ServiceHeartbeatRecord o1, ServiceHeartbeatRecord o2) {
                    int diffTime = o1.lastSentTime.compareTo(o2.lastSentTime);
                    return diffTime != 0 ? diffTime : o1.serviceId.compareTo(o2.serviceId);
                }
            });

    private TaskScheduler scheduler;
    private HeartbeatProperties configuration;
    private ConsulClient client;
    private HealthIndicator healthIndicator;

    public TtlScheduler(TaskScheduler scheduler, HeartbeatProperties configuration, ConsulClient client, HealthIndicator healthIndicator) {
        this.scheduler = scheduler;
        this.configuration = configuration;
        this.client = client;
        this.healthIndicator = healthIndicator;
    }

    /**
     * Add a service to the checks loop.
     */
    public void add(final NewService service) {
        serviceHeartbeats.add(new ServiceHeartbeatRecord(service.getId()));
        scheduleNextHeartbeatRound();
    }

    private void doHeartbeatServices() {
        for (ServiceHeartbeatRecord serviceRec : serviceHeartbeats) {
            DateTime latestHeartbeatDoneForService = serviceRec.lastSentTime;
            if (latestHeartbeatDoneForService.plus(configuration.getHeartbeatExpirePeriod())
                    .isBefore(now())) {
                String checkId = serviceRec.serviceId;
                if (!checkId.startsWith("service:")) {
                    checkId = "service:" + checkId;
                }

                computeAndSendStatus(checkId);
                log.debug("Sending consul heartbeat for: {}", serviceRec);
                serviceHeartbeats.remove(serviceRec);
                serviceHeartbeats.add(new ServiceHeartbeatRecord(serviceRec.serviceId));
            }
        }
        scheduleNextHeartbeatRound();
    }

    private void scheduleNextHeartbeatRound() {
        if (!serviceHeartbeats.isEmpty()) {
            scheduler.schedule(
                    new Runnable() {
                        @Override
                        public void run() {
                            doHeartbeatServices();
                        }
                    },
                    nextSendTime().toDate());
        }
    }

    private DateTime nextSendTime() {
        return serviceHeartbeats.first().nextSendTime();
    }

    private DateTime now() {
        return DateTime.now();
    }

    private void computeAndSendStatus(String checkId) {
        Health health = healthIndicator.health();
        Status status = health.getStatus();
        Map<String, Object> details = new HashMap<>(health.getDetails());
        details.put("overallStatus", status);
        details.put("dateTime", now());
        String note = details.toString();
        if (Status.UP.equals(status)) {
            client.agentCheckPass(checkId, note);
        } else {
            if (Status.UNKNOWN.equals(status)) {
                client.agentCheckWarn(checkId, note);
            } else {
                client.agentCheckFail(checkId, note);
            }
        }
    }

    public void removeAll() {
        //todo see how we can cancel existing jobs & shutdown, or enforce shutdown ordering
        serviceHeartbeats.removeAll(serviceHeartbeats);
    }

    private class ServiceHeartbeatRecord {
        private String serviceId;
        private DateTime lastSentTime;

        public ServiceHeartbeatRecord(String serviceId, DateTime lastSentTime) {
            this.serviceId = serviceId;
            this.lastSentTime = lastSentTime;
        }

        public ServiceHeartbeatRecord(String serviceId) {
            this(serviceId, now());
        }

        public DateTime nextSendTime() {
            DateTime time = lastSentTime.plus(configuration.getHeartbeatExpirePeriod());
            log.debug("Computed next send time = {}", time);
            return time;
        }
    }
}