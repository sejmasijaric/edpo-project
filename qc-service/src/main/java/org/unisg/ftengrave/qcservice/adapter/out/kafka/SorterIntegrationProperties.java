package org.unisg.ftengrave.qcservice.adapter.out.kafka;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.sorter")
public class SorterIntegrationProperties {

    private final Map<String, String> eventTypes = new HashMap<>();

    public Map<String, String> getEventTypes() {
        return eventTypes;
    }

    public String getEventType(String sink) {
        String eventType = eventTypes.get(sink);
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalStateException("No sorter event type configured for sink: " + sink);
        }
        return eventType;
    }
}
