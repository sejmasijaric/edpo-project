package org.unisg.ftengrave.intakeservice.adapter.out.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.vacuum-gripper")
@Getter
@Setter
public class VacuumGripperIntegrationProperties {

    private CommandTypes commandTypes = new CommandTypes();

    public String moveToEngraverCommandType() {
        return commandTypes.getMoveToEngraver();
    }

    @Getter
    @Setter
    public static class CommandTypes {
        private String moveToEngraver;
    }
}
