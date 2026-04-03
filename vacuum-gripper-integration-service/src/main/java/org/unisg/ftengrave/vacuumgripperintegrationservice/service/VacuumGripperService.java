package org.unisg.ftengrave.vacuumgripperintegrationservice.service;

import org.springframework.stereotype.Service;
import org.unisg.ftengrave.vacuumgripperintegrationservice.dto.VacuumGripperCommandDto;

@Service
public class VacuumGripperService {

  public static final String MOVE_ITEM_FROM_INPUT_TO_ENGRAVER_COMMAND =
      "move-item-from-input-to-engraver-command";

  private final VacuumGripperHttpService vacuumGripperHttpService;

  public VacuumGripperService(VacuumGripperHttpService vacuumGripperHttpService) {
    this.vacuumGripperHttpService = vacuumGripperHttpService;
  }

  public void handle(VacuumGripperCommandDto vacuumGripperCommandDto) {
    if (MOVE_ITEM_FROM_INPUT_TO_ENGRAVER_COMMAND.equals(vacuumGripperCommandDto.getCommandType())) {
      vacuumGripperHttpService.moveItemFromInputToEngraver();
    }
  }
}
