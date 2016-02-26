package org.springframework.cloud.consul.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.net.HttpURLConnection;
import java.net.URL;


public class ConsulConnectionChecker implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {

        Environment environment = conditionContext.getEnvironment();
        String host = environment.getProperty("spring.cloud.consul.host");
        String port = environment.getProperty("spring.cloud.consul.port");
        try {
            URL url = new URL("http://" + host + ":" + port);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int code = connection.getResponseCode();

            return code == 200;
        } catch (Exception e) {

            return false;
        }

    }
}
