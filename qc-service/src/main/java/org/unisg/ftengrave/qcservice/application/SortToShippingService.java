package org.unisg.ftengrave.qcservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.qcservice.port.in.SortToShippingUseCase;
import org.unisg.ftengrave.qcservice.port.out.SortToShippingPort;

@Service
@RequiredArgsConstructor
public class SortToShippingService implements SortToShippingUseCase {

    private final SortToShippingPort sortToShippingPort;

    @Override
    public void sortToShipping() {
        sortToShippingPort.publish();
    }
}
