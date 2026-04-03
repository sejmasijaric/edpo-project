package org.unisg.ftengrave.qcservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.qcservice.port.in.SortToRetryUseCase;
import org.unisg.ftengrave.qcservice.port.out.SortToRetryPort;

@Service
@RequiredArgsConstructor
public class SortToRetryService implements SortToRetryUseCase {

    private final SortToRetryPort sortToRetryPort;

    @Override
    public void sortToRetry() {
        sortToRetryPort.publish();
    }
}
