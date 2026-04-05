package org.unisg.ftengrave.qcservice.adapter.out.kafka;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.sorter")
public class SorterIntegrationProperties {

    private final Map<String, String> commandTypes = new HashMap<>();

    public Map<String, String> getCommandTypes() {
        return commandTypes;
    }

    public String getCommandType(String sink) {
        String commandType = commandTypes.get(sink);
        if (commandType == null || commandType.isBlank()) {
            throw new IllegalStateException("No sorter command type configured for sink: " + sink);
        }
        return commandType;
    }
}
