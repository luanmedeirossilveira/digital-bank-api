package com.example.digitalbankapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Dados para transferência entre contas")
public record TransferRequest(

        @Schema(description = "Id da conta de origem", example = "1")
        @NotNull(message = "conta de origem é obrigatória")
        Long sourceAccountId,

        @Schema(description = "Id da conta de destino", example = "2")
        @NotNull(message = "conta de destino é obrigatória")
        Long destinationAccountId,

        @Schema(description = "Valor a transferir", example = "150.00")
        @NotNull(message = "valor é obrigatório")
        @DecimalMin(value = "0.01", message = "valor deve ser maior que zero")
        @Digits(integer = 17, fraction = 2, message = "valor deve ter no máximo 2 casas decimais")
        BigDecimal amount
) {
}
