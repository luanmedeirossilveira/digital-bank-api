package com.example.digitalbankapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Resposta padrão de erro da API")
public record ErrorResponse(

        @Schema(description = "Momento em que o erro ocorreu")
        OffsetDateTime timestamp,

        @Schema(description = "Código HTTP", example = "404")
        int status,

        @Schema(description = "Descrição do código HTTP", example = "Not Found")
        String error,

        @Schema(description = "Mensagem legível do erro", example = "Conta não encontrada: id=99")
        String message,

        @Schema(description = "Detalhes adicionais, como erros de validação por campo")
        List<String> details
) {

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(OffsetDateTime.now(), status, error, message, List.of());
    }

    public static ErrorResponse of(int status, String error, String message, List<String> details) {
        return new ErrorResponse(OffsetDateTime.now(), status, error, message, details);
    }
}
