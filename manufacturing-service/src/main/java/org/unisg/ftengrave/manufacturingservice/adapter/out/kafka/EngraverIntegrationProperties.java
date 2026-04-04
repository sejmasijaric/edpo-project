package org.unisg.ftengrave.manufacturingservice.adapter.out.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.engraver")
@Getter
@Setter
public class EngraverIntegrationProperties {

    private CommandTypes commandTypes = new CommandTypes();

    public String runEngravingCommandType() {
        return commandTypes.getRunEngraving();
    }

    @Getter
    @Setter
    public static class CommandTypes {
        private String runEngraving;
    }
}
