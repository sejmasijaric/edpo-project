package org.unisg.ftengrave.qcservice.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.qcservice.domain.ItemColor;

class MatchItemColorServiceTest {

    private final MatchItemColorService service = new MatchItemColorService();

    @Test
    void matchesReturnsTrueWhenColorsAreEqual() {
        assertThat(service.matches(ItemColor.RED, ItemColor.RED)).isTrue();
    }

    @Test
    void matchesReturnsFalseWhenColorsDiffer() {
        assertThat(service.matches(ItemColor.RED, ItemColor.BLUE)).isFalse();
    }
}
