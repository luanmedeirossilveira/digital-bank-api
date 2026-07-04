package com.example.digitalbankapi.dto;

import com.example.digitalbankapi.entity.Account;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Dados de uma conta")
public record AccountResponse(

        @Schema(description = "Identificador da conta", example = "1")
        Long id,

        @Schema(description = "Nome do titular da conta", example = "Maria Silva")
        String name,

        @Schema(description = "Saldo atual da conta", example = "1000.00")
        BigDecimal balance
) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getName(), account.getBalance());
    }
}
