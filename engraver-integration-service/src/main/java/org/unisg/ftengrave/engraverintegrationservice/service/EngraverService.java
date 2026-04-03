package org.unisg.ftengrave.engraverintegrationservice.service;

import org.springframework.stereotype.Service;
import org.unisg.ftengrave.engraverintegrationservice.dto.EngraverCommandDto;

@Service
public class EngraverService {

  public static final String RUN_ENGRAVING_COMMAND = "run-engraving-command";

  private final EngraverHttpService engraverHttpService;

  public EngraverService(EngraverHttpService engraverHttpService) {
    this.engraverHttpService = engraverHttpService;
  }

  public void handle(EngraverCommandDto engraverCommandDto) {
    if (RUN_ENGRAVING_COMMAND.equals(engraverCommandDto.getCommandType())) {
      engraverHttpService.runEngraving();
    }
  }
}
