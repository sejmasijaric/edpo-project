package org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.unisg.ftengrave.qcservice.domain.ItemColor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageProcessDto implements Serializable {

    private String itemIdentifier;
    @JsonProperty("target-color")
    private ItemColor targetColor;
}
