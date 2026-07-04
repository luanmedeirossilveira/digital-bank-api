package com.example.digitalbankapi.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    @Test
    void shouldNormalizeToTwoDecimalPlaces() {
        assertThat(Money.normalize(new BigDecimal("10"))).isEqualTo(new BigDecimal("10.00"));
        assertThat(Money.normalize(new BigDecimal("10.5"))).isEqualTo(new BigDecimal("10.50"));
    }

    @Test
    void shouldRoundHalfUp() {
        assertThat(Money.normalize(new BigDecimal("10.005"))).isEqualTo(new BigDecimal("10.01"));
        assertThat(Money.normalize(new BigDecimal("10.004"))).isEqualTo(new BigDecimal("10.00"));
    }
}
