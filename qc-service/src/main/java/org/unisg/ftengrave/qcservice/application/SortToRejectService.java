package org.unisg.ftengrave.qcservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.qcservice.port.in.SortToRejectUseCase;
import org.unisg.ftengrave.qcservice.port.out.SortToRejectPort;

@Service
@RequiredArgsConstructor
public class SortToRejectService implements SortToRejectUseCase {

    private final SortToRejectPort sortToRejectPort;

    @Override
    public void sortToReject() {
        sortToRejectPort.publish();
    }
}
