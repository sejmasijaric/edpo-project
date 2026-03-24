package org.unisg.ftengrave.factorysimulator.controller;

import org.unisg.ftengrave.factorysimulator.service.FactorySimulatorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FactoryViewController {

  private final FactorySimulatorService factorySimulatorService;

  public FactoryViewController(FactorySimulatorService factorySimulatorService) {
    this.factorySimulatorService = factorySimulatorService;
  }

  @GetMapping("/")
  public String index(Model model) {
    model.addAttribute("factoryWidth", 980);
    model.addAttribute("factoryHeight", 620);
    model.addAttribute("sinks", factorySimulatorService.getSinks());
    return "index";
  }
}
