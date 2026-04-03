package org.unisg.ftengrave.qcservice.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.qcservice.domain.ItemColor;
import org.unisg.ftengrave.qcservice.port.in.HandleColorDetectedEventUseCase;
import org.unisg.ftengrave.qcservice.port.in.SortingMachineEvent;
import org.unisg.ftengrave.qcservice.port.out.CorrelateMessagePort;
import org.unisg.ftengrave.qcservice.port.out.ResolveWaitingMessageBusinessKeyPort;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ColorDetectedEventService implements HandleColorDetectedEventUseCase {

    static final String COLOR_DETECTED_MESSAGE = "ColorDetectedMessage";
    private static final String COLOR_DETECTED_EVENT = "color-detected";

    private final ResolveWaitingMessageBusinessKeyPort resolveWaitingMessageBusinessKeyPort;
    private final CorrelateMessagePort correlateMessagePort;

    @Override
    public void handle(SortingMachineEvent event) {
        ItemColor color = mapDetectedColor(event);
        if (color == null) {
            return;
        }

        String itemIdentifier = resolveWaitingMessageBusinessKeyPort.resolve(COLOR_DETECTED_MESSAGE);
        if (itemIdentifier == null) {
            return;
        }

        correlateMessagePort.correlateMessage(
                COLOR_DETECTED_MESSAGE,
                itemIdentifier,
                Map.of(
                        "itemIdentifier", itemIdentifier,
                        "detected-color", color));
    }

    private ItemColor mapDetectedColor(SortingMachineEvent event) {
        if (event == null || event.eventType() == null) {
            log.info("Ignoring sorting-machine event without eventType");
            return null;
        }

        if (!COLOR_DETECTED_EVENT.equals(event.eventType())) {
            log.info("Ignoring unsupported sorting-machine event {}", event.eventType());
            return null;
        }

        if (event.color() == null || event.color().isBlank()) {
            log.info("Ignoring color-detected event without color payload");
            return null;
        }

        ItemColor detectedColor = ItemColor.fromExternalValue(event.color());
        if (detectedColor == null) {
            log.info("Ignoring color-detected event with unsupported color {}", event.color());
        }
        return detectedColor;
    }
}
