package org.unisg.ftengrave.dashboardservice.dto;

import java.util.HashMap;
import java.util.Map;

public class ItemState {

  private String itemIdentifier;
  private String currentStage;
  private boolean terminal;
  private Long productionStartTimestamp;
  private Long endToEndStartTimestamp;
  private Long terminalTimestamp;
  private String terminalOutcome;
  private boolean manualInterventionOpen;
  private String openTaskName;
  private final Map<String, Integer> attemptCounts = new HashMap<>();
  private int retryCount;

  public ItemState() {
  }

  public ItemState(String itemIdentifier) {
    this.itemIdentifier = itemIdentifier;
  }

  public String getItemIdentifier() {
    return itemIdentifier;
  }

  public void setItemIdentifier(String itemIdentifier) {
    this.itemIdentifier = itemIdentifier;
  }

  public String getCurrentStage() {
    return currentStage;
  }

  public void setCurrentStage(String currentStage) {
    this.currentStage = currentStage;
  }

  public boolean isTerminal() {
    return terminal;
  }

  public void setTerminal(boolean terminal) {
    this.terminal = terminal;
  }

  public Long getProductionStartTimestamp() {
    return productionStartTimestamp;
  }

  public void setProductionStartTimestamp(Long productionStartTimestamp) {
    this.productionStartTimestamp = productionStartTimestamp;
  }

  public Long getEndToEndStartTimestamp() {
    return endToEndStartTimestamp;
  }

  public void setEndToEndStartTimestamp(Long endToEndStartTimestamp) {
    this.endToEndStartTimestamp = endToEndStartTimestamp;
  }

  public Long getTerminalTimestamp() {
    return terminalTimestamp;
  }

  public void setTerminalTimestamp(Long terminalTimestamp) {
    this.terminalTimestamp = terminalTimestamp;
  }

  public String getTerminalOutcome() {
    return terminalOutcome;
  }

  public void setTerminalOutcome(String terminalOutcome) {
    this.terminalOutcome = terminalOutcome;
  }

  public boolean isManualInterventionOpen() {
    return manualInterventionOpen;
  }

  public void setManualInterventionOpen(boolean manualInterventionOpen) {
    this.manualInterventionOpen = manualInterventionOpen;
  }

  public String getOpenTaskName() {
    return openTaskName;
  }

  public void setOpenTaskName(String openTaskName) {
    this.openTaskName = openTaskName;
  }

  public Map<String, Integer> getAttemptCounts() {
    return attemptCounts;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(int retryCount) {
    this.retryCount = retryCount;
  }
}
