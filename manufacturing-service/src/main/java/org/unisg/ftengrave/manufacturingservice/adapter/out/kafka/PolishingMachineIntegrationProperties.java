package org.unisg.ftengrave.manufacturingservice.adapter.out.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.polishing-machine")
@Getter
@Setter
public class PolishingMachineIntegrationProperties {

    private CommandTypes commandTypes = new CommandTypes();

    public String runPolishingCommandType() {
        return commandTypes.getRunPolishing();
    }

    @Getter
    @Setter
    public static class CommandTypes {
        private String runPolishing;
    }
}
