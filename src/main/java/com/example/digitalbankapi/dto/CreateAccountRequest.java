package com.example.digitalbankapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Dados para criação de uma conta")
public record CreateAccountRequest(

        @Schema(description = "Nome do titular da conta", example = "Maria Silva")
        @NotBlank(message = "nome é obrigatório")
        @Size(max = 120, message = "nome deve ter no máximo 120 caracteres")
        String name,

        @Schema(description = "Saldo inicial da conta", example = "1000.00")
        @NotNull(message = "saldo inicial é obrigatório")
        @DecimalMin(value = "0.00", message = "saldo inicial não pode ser negativo")
        @Digits(integer = 17, fraction = 2, message = "saldo inicial deve ter no máximo 2 casas decimais")
        BigDecimal initialBalance
) {
}
