package org.unisg.ftengrave.qcservice.application;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.unisg.ftengrave.qcservice.port.out.RequestColorDetectionPort;
import org.unisg.ftengrave.qcservice.port.out.SortToRejectPort;
import org.unisg.ftengrave.qcservice.port.out.SortToRetryPort;
import org.unisg.ftengrave.qcservice.port.out.SortToShippingPort;

class CommandDispatchServicesTest {

    @Test
    void requestColorDetectionPublishesThroughOutputPort() {
        RequestColorDetectionPort port = Mockito.mock(RequestColorDetectionPort.class);

        new RequestColorDetectionService(port).requestColorDetection();

        verify(port).publish();
    }

    @Test
    void sortToRejectPublishesThroughOutputPort() {
        SortToRejectPort port = Mockito.mock(SortToRejectPort.class);

        new SortToRejectService(port).sortToReject();

        verify(port).publish();
    }

    @Test
    void sortToRetryPublishesThroughOutputPort() {
        SortToRetryPort port = Mockito.mock(SortToRetryPort.class);

        new SortToRetryService(port).sortToRetry();

        verify(port).publish();
    }

    @Test
    void sortToShippingPublishesThroughOutputPort() {
        SortToShippingPort port = Mockito.mock(SortToShippingPort.class);

        new SortToShippingService(port).sortToShipping();

        verify(port).publish();
    }
}
