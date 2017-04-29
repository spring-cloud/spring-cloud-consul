package org.springframework.cloud.consul.discovery;

import org.springframework.cloud.bootstrap.BootstrapApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * @author Alex Antonov
 */
public class ConsulTagsPropertySource extends EnumerablePropertySource {
    public static final String TAG_PROPERTY_NAME = "spring.cloud.consul.discovery.tags";

    private ConfigurableEnvironment environment;

    public ConsulTagsPropertySource(String name, ConfigurableEnvironment environment) {
        super(name);
        this.environment = environment;
    }

    @Override
    public String[] getPropertyNames() {
        return new String[]{TAG_PROPERTY_NAME};
    }

    @Override
    public Object getProperty(String name) {
        if (!TAG_PROPERTY_NAME.equals(name)) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder();
            boolean isFirst = true;

            for (PropertySource<?> propertySource : environment.getPropertySources()) {
                if (!ignorePropertySource(propertySource) && propertySource.containsProperty(TAG_PROPERTY_NAME)) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        sb.append(",");
                    }
                    Object propertyValue = propertySource.getProperty(TAG_PROPERTY_NAME);
                    sb.append(convertToString(propertyValue));
                }
            }

            return sb.toString();
        }
    }

    protected boolean ignorePropertySource(PropertySource propertySource) {
        return (propertySource == this
                || BootstrapApplicationListener.DEFAULT_PROPERTIES.equals(propertySource.getName()));
    }

    private String convertToString(Object property) {
        if (property instanceof Iterable) {
            Collection collection = (Collection) property;
            return StringUtils.collectionToCommaDelimitedString(collection);
        } else {
            return String.valueOf(property);
        }
    }
}
