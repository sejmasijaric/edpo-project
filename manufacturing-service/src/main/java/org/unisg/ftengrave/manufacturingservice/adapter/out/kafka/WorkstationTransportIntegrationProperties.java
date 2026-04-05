package org.unisg.ftengrave.manufacturingservice.adapter.out.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.workstation-transport")
@Getter
@Setter
public class WorkstationTransportIntegrationProperties {

    private CommandTypes commandTypes = new CommandTypes();

    public String moveToPolishingMachineCommandType() {
        return commandTypes.getMoveToPolishingMachine();
    }

    @Getter
    @Setter
    public static class CommandTypes {
        private String moveToPolishingMachine;
    }
}
