package org.unisg.ftengrave.qcservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.qcservice.port.in.RequestColorDetectionUseCase;
import org.unisg.ftengrave.qcservice.port.out.RequestColorDetectionPort;

@Service
@RequiredArgsConstructor
public class RequestColorDetectionService implements RequestColorDetectionUseCase {

    private final RequestColorDetectionPort requestColorDetectionPort;

    @Override
    public void requestColorDetection() {
        requestColorDetectionPort.publish();
    }
}
