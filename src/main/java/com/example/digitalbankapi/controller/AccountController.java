package com.example.digitalbankapi.controller;

import com.example.digitalbankapi.dto.AccountResponse;
import com.example.digitalbankapi.dto.CreateAccountRequest;
import com.example.digitalbankapi.dto.ErrorResponse;
import com.example.digitalbankapi.dto.MovementResponse;
import com.example.digitalbankapi.entity.Account;
import com.example.digitalbankapi.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Contas", description = "Gestão de contas do banco digital")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Cria uma conta", description = "Cria uma conta com nome do titular e saldo inicial")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        Account account = accountService.create(request);
        URI location = URI.create("/api/v1/accounts/" + account.getId());
        return ResponseEntity.created(location).body(AccountResponse.from(account));
    }

    @GetMapping
    @Operation(summary = "Lista todas as contas")
    @ApiResponse(responseCode = "200", description = "Lista de contas")
    public List<AccountResponse> list() {
        return accountService.findAll().stream()
                .map(AccountResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta uma conta pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta encontrada"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public AccountResponse get(@PathVariable Long id) {
        return AccountResponse.from(accountService.getById(id));
    }

    @GetMapping("/{id}/movements")
    @Operation(summary = "Consulta as movimentações de uma conta",
            description = "Retorna o histórico de movimentações (débitos e créditos) da conta, da mais recente para a mais antiga")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico de movimentações"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<MovementResponse> movements(@PathVariable Long id) {
        return accountService.getMovements(id).stream()
                .map(MovementResponse::from)
                .toList();
    }
}
