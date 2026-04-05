package org.unisg.ftengrave.sorterintegrationservice.dto;

public class SortingMachineEventTransformationDto {

  private String timestamp;
  private int i1LightBarrier;
  private int i2ColorSensor;
  private int i3LightBarrier;
  private int i6LightBarrier;
  private int i7LightBarrier;
  private int i8LightBarrier;
  private String currentTask;
  private double currentTaskDuration;

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public int getI1LightBarrier() {
    return i1LightBarrier;
  }

  public void setI1LightBarrier(int i1LightBarrier) {
    this.i1LightBarrier = i1LightBarrier;
  }

  public int getI2ColorSensor() {
    return i2ColorSensor;
  }

  public void setI2ColorSensor(int i2ColorSensor) {
    this.i2ColorSensor = i2ColorSensor;
  }

  public int getI3LightBarrier() {
    return i3LightBarrier;
  }

  public void setI3LightBarrier(int i3LightBarrier) {
    this.i3LightBarrier = i3LightBarrier;
  }

  public int getI6LightBarrier() {
    return i6LightBarrier;
  }

  public void setI6LightBarrier(int i6LightBarrier) {
    this.i6LightBarrier = i6LightBarrier;
  }

  public int getI7LightBarrier() {
    return i7LightBarrier;
  }

  public void setI7LightBarrier(int i7LightBarrier) {
    this.i7LightBarrier = i7LightBarrier;
  }

  public int getI8LightBarrier() {
    return i8LightBarrier;
  }

  public void setI8LightBarrier(int i8LightBarrier) {
    this.i8LightBarrier = i8LightBarrier;
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
