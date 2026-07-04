package com.example.digitalbankapi.dto;

import com.example.digitalbankapi.entity.AccountMovement;
import com.example.digitalbankapi.entity.MovementType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Movimentação financeira de uma conta")
public record MovementResponse(

        @Schema(description = "Identificador da movimentação", example = "1")
        Long id,

        @Schema(description = "Id da conta movimentada", example = "1")
        Long accountId,

        @Schema(description = "Tipo da movimentação", example = "DEBIT")
        MovementType type,

        @Schema(description = "Valor da movimentação", example = "150.00")
        BigDecimal amount,

        @Schema(description = "Id da transferência que originou a movimentação", example = "1")
        Long transferId,

        @Schema(description = "Data/hora da movimentação")
        Instant createdAt
) {

    public static MovementResponse from(AccountMovement movement) {
        return new MovementResponse(
                movement.getId(),
                movement.getAccount().getId(),
                movement.getType(),
                movement.getAmount(),
                movement.getTransfer().getId(),
                movement.getCreatedAt());
    }
}
