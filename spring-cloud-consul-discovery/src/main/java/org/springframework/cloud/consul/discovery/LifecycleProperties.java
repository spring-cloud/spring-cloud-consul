package org.springframework.cloud.consul.discovery;

import lombok.Data;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Aleksandr Tarasov (aatarasoff)
 */
@ConfigurationProperties(prefix = "spring.cloud.consul.discovery.lifecycle")
@Data
public class LifecycleProperties {
	private boolean enabled = true;
}