package org.unisg.ftengrave.workstationtransportintegrationservice.service;

import org.springframework.stereotype.Service;
import org.unisg.ftengrave.workstationtransportintegrationservice.dto.WorkstationTransportCommandDto;

@Service
public class WorkstationTransportService {

  public static final String MOVE_ITEM_FROM_ENGRAVER_TO_POLISHING_MACHINE_COMMAND =
      "move-item-from-engraver-to-polishing-machine-command";

  private final WorkstationTransportHttpService workstationTransportHttpService;

  public WorkstationTransportService(
      WorkstationTransportHttpService workstationTransportHttpService) {
    this.workstationTransportHttpService = workstationTransportHttpService;
  }

  public void handle(WorkstationTransportCommandDto workstationTransportCommandDto) {
    if (MOVE_ITEM_FROM_ENGRAVER_TO_POLISHING_MACHINE_COMMAND.equals(
        workstationTransportCommandDto.getCommandType())) {
      workstationTransportHttpService.moveItemFromEngraverToPolishingMachine();
    }
  }
}
