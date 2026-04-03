package org.unisg.ftengrave.orderorchestrator;

public class DuplicateBusinessKeyException extends RuntimeException {

    public DuplicateBusinessKeyException(String orderIdentifier, Throwable cause) {
        super("A running process instance already exists for orderIdentifier " + orderIdentifier, cause);
    }
}
