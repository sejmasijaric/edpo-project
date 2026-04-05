package org.unisg.ftengrave.workstationtransportintegrationservice.dto;

public class WorkstationTransportEventTransformationDto {

  private String timestamp;
  private String currentState;
  private String currentTask;
  private double currentTaskDuration;

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getCurrentState() {
    return currentState;
  }

  public void setCurrentState(String currentState) {
    this.currentState = currentState;
  }

  public String getCurrentTask() {
    return currentTask;
  }

  public void setCurrentTask(String currentTask) {
    this.currentTask = currentTask;
  }

  public double getCurrentTaskDuration() {
    return currentTaskDuration;
  }

  public void setCurrentTaskDuration(double currentTaskDuration) {
    this.currentTaskDuration = currentTaskDuration;
  }
}
