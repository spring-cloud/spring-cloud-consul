package org.springframework.cloud.consul.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * @author Alex Antonov
 */
// This is set to the lowest precedence in order to make sure that this processor runs last, thus adding
//  ConsulTagsPropertySource to the very top of the precedence chain, above all the other PropertySources
@Order
public class ConsulTagsEnvironmentPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        environment.getPropertySources().addFirst(createPropertySource(environment));
    }

    private PropertySource createPropertySource(ConfigurableEnvironment environment) {
        return new ConsulTagsPropertySource("consulTags", environment);
    }
}
