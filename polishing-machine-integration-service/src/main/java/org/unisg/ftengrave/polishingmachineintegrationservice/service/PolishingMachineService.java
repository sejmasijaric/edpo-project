package org.unisg.ftengrave.polishingmachineintegrationservice.service;

import org.springframework.stereotype.Service;
import org.unisg.ftengrave.polishingmachineintegrationservice.dto.PolishingMachineCommandDto;

@Service
public class PolishingMachineService {

  public static final String RUN_POLISHING_COMMAND = "run-polishing-command";

  private final PolishingMachineHttpService polishingMachineHttpService;

  public PolishingMachineService(PolishingMachineHttpService polishingMachineHttpService) {
    this.polishingMachineHttpService = polishingMachineHttpService;
  }

  public void handle(PolishingMachineCommandDto polishingMachineCommandDto) {
    if (RUN_POLISHING_COMMAND.equals(polishingMachineCommandDto.getCommandType())) {
      polishingMachineHttpService.runPolishing();
    }
  }
}
