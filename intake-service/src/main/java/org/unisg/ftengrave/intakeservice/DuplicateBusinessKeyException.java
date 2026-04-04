package org.unisg.ftengrave.intakeservice;

public class DuplicateBusinessKeyException extends RuntimeException {

    public DuplicateBusinessKeyException(String businessKey, Throwable cause) {
        super("A running intake process already exists for business key '%s'".formatted(businessKey), cause);
    }
}
