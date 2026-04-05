package org.unisg.ftengrave.polishingmachineintegrationservice.dto;

public class PolishingMachineEventTransformationDto {

  private String timestamp;
  private int i4LightBarrier;
  private String currentTask;
  private double currentTaskDuration;

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public int getI4LightBarrier() {
    return i4LightBarrier;
  }

  public void setI4LightBarrier(int i4LightBarrier) {
    this.i4LightBarrier = i4LightBarrier;
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
