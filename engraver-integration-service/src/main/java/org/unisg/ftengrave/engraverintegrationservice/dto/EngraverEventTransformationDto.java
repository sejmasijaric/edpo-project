package org.unisg.ftengrave.engraverintegrationservice.dto;

public class EngraverEventTransformationDto {

  private String timestamp;
  private int i5LightBarrier;
  private String currentTask;
  private double currentTaskDuration;

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public int getI5LightBarrier() {
    return i5LightBarrier;
  }

  public void setI5LightBarrier(int i5LightBarrier) {
    this.i5LightBarrier = i5LightBarrier;
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
