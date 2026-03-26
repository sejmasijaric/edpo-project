package org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.dto;

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
public class ColorDetectedMessageProcessDto implements Serializable {

    private String itemIdentifier;
    private ItemColor color;
}
