package org.unisg.ftengrave.qcservice.port.out;

public interface PublishUserTaskIssuedEventPort {

    void publish(String itemIdentifier, String commandType, String taskName, String taskCategory, String stationName,
            String targetColor);
}
