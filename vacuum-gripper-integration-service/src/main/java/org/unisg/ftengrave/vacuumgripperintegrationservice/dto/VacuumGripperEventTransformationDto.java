package org.unisg.ftengrave.vacuumgripperintegrationservice.dto;

public class VacuumGripperEventTransformationDto {

  private String timestamp;
  private int i7LightBarrier;
  private int i4LightBarrier;
  private String currentTask;
  private double currentTaskDuration;

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public int getI7LightBarrier() {
    return i7LightBarrier;
  }

  public void setI7LightBarrier(int i7LightBarrier) {
    this.i7LightBarrier = i7LightBarrier;
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
