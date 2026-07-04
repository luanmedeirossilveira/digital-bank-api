package com.example.digitalbankapi.dto;

import com.example.digitalbankapi.entity.Transfer;
import com.example.digitalbankapi.entity.TransferStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Dados de uma transferência realizada")
public record TransferResponse(

        @Schema(description = "Identificador da transferência", example = "1")
        Long id,

        @Schema(description = "Id da conta de origem", example = "1")
        Long sourceAccountId,

        @Schema(description = "Id da conta de destino", example = "2")
        Long destinationAccountId,

        @Schema(description = "Valor transferido", example = "150.00")
        BigDecimal amount,

        @Schema(description = "Status da transferência", example = "COMPLETED")
        TransferStatus status,

        @Schema(description = "Motivo da falha, presente apenas quando status = FAILED", example = "Saldo insuficiente")
        String failureReason,

        @Schema(description = "Data/hora da transferência")
        Instant createdAt
) {

    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getSourceAccount().getId(),
                transfer.getDestinationAccount().getId(),
                transfer.getAmount(),
                transfer.getStatus(),
                transfer.getFailureReason(),
                transfer.getCreatedAt());
    }
}
