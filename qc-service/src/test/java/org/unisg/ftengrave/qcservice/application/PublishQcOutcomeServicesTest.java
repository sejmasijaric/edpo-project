package org.unisg.ftengrave.qcservice.application;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.unisg.ftengrave.qcservice.port.out.PublishQcRejectionOutcomePort;
import org.unisg.ftengrave.qcservice.port.out.PublishQcShippingOutcomePort;

import static org.mockito.Mockito.verify;

class PublishQcOutcomeServicesTest {

    @Test
    void shippingServiceDelegatesToPort() {
        PublishQcShippingOutcomePort port = Mockito.mock(PublishQcShippingOutcomePort.class);

        new PublishQcShippingOutcomeService(port).publish("item-42");

        verify(port).publish("item-42");
    }

    @Test
    void rejectionServiceDelegatesToPort() {
        PublishQcRejectionOutcomePort port = Mockito.mock(PublishQcRejectionOutcomePort.class);

        new PublishQcRejectionOutcomeService(port).publish("item-42");

        verify(port).publish("item-42");
    }
}
