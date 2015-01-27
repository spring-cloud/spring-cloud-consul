package org.springframework.cloud.consul.sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.cloud.bus.jackson.SubtypeModule;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.consul.bus.SimpleRemoteEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Spencer Gibb
 */
@Configuration
@EnableAutoConfiguration
@EnableDiscoveryClient
@RestController
@Slf4j
public class SampleApplication implements ApplicationListener<SimpleRemoteEvent> {

    public static final String CLIENT_NAME = "testConsulApp";

    @Autowired
    LoadBalancerClient loadBalancer;

    @Autowired
    DiscoveryClient discoveryClient;

    @Autowired
    Environment env;

    @Autowired(required = false)
    RelaxedPropertyResolver resolver;

    @RequestMapping("/me")
    public ServiceInstance me() {
        return discoveryClient.getLocalServiceInstance();
    }

    @RequestMapping("/")
    public ServiceInstance lb() {
        return loadBalancer.choose(CLIENT_NAME);
    }

    @RequestMapping("/myenv")
    public String env(@RequestParam("prop") String prop) {
        String property = new RelaxedPropertyResolver(env).getProperty(prop, "Not Found");
        return property;
    }

    @Bean
    public SubtypeModule sampleSubtypeModule() {
        return new SubtypeModule(SimpleRemoteEvent.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }

    @Override
    public void onApplicationEvent(SimpleRemoteEvent event) {
        log.info("Received event: {}", event);
    }
}
