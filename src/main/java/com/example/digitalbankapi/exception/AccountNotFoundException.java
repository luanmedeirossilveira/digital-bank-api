package com.example.digitalbankapi.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long id) {
        super("Conta não encontrada: id=" + id);
    }
}
