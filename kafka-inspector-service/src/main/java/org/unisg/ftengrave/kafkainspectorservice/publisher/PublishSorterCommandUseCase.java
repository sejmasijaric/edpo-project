package org.unisg.ftengrave.kafkainspectorservice.publisher;

public interface PublishSorterCommandUseCase {

  void publish(String commandName);
}
