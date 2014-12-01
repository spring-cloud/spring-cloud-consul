package org.springframework.cloud.consul;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Spencer Gibb
 */
@ConfigurationProperties("consul")
@Data
public class ConsulProperties {
    @NotNull
    private String url = "http://localhost:8500";

    private List<String> tags = new ArrayList<>();

    private boolean enabled = true;

    private String prefix = "config";

    private List<String> managementTags = Arrays.asList("management");
}
