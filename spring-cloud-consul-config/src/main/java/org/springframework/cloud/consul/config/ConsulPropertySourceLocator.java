package org.springframework.cloud.consul.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.client.PropertySourceLocator;
import org.springframework.cloud.consul.ConsulProperties;
import org.springframework.cloud.consul.client.KeyValueClient;
import org.springframework.core.env.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Spencer Gibb
 */
public class ConsulPropertySourceLocator implements PropertySourceLocator {

    @Autowired
    private KeyValueClient keyValueClient;

    @Autowired
    private ConsulProperties properties;

    @Override
    public PropertySource<?> locate(Environment environment) {
        if (environment instanceof ConfigurableEnvironment) {
            ConfigurableEnvironment env = (ConfigurableEnvironment) environment;
            String appName = env.getProperty("spring.application.name");
            List<String> profiles = Arrays.asList(env.getActiveProfiles());

            String prefix = properties.getPrefix();
            List<String> contexts = new ArrayList<>();

            String defaultContext = prefix + "/application";
            contexts.add(defaultContext + "/");
            addProfiles(contexts, defaultContext, profiles);

            String baseContext = prefix + "/" + appName;
            contexts.add(baseContext + "/");
            addProfiles(contexts, baseContext, profiles);

            CompositePropertySource composite = new CompositePropertySource("consul");

            for (String propertySourceContext : contexts) {
                ConsulPropertySource propertySource = create(propertySourceContext);
                propertySource.init();
                composite.addPropertySource(propertySource);
            }

            return composite;
        }
        return null;
    }

    private ConsulPropertySource create(String context) {
        return new ConsulPropertySource(context, keyValueClient);
    }

    private void addProfiles(List<String> contexts, String baseContext, List<String> profiles) {
        for (String profile : profiles) {
            contexts.add(baseContext + "::" + profile + "/");
        }
    }
}
