package org.unisg.ftengrave.manufacturingservice;

public class DuplicateBusinessKeyException extends RuntimeException {

    public DuplicateBusinessKeyException(String businessKey, Throwable cause) {
        super("A running manufacturing process already exists for business key '%s'".formatted(businessKey), cause);
    }
}
