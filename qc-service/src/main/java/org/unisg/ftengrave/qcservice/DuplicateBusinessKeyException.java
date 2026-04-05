package org.unisg.ftengrave.qcservice;

public class DuplicateBusinessKeyException extends RuntimeException {

    public DuplicateBusinessKeyException(String businessKey, Throwable cause) {
        super("A running QC process already exists for business key '%s'".formatted(businessKey), cause);
    }
}
