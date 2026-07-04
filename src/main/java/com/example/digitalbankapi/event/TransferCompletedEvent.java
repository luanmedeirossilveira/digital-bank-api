package com.example.digitalbankapi.event;

import java.math.BigDecimal;

public record TransferCompletedEvent(
        Long transferId,
        Long sourceAccountId,
        Long destinationAccountId,
        BigDecimal amount
) {
}
