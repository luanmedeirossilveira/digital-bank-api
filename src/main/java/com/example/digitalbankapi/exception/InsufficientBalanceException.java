package com.example.digitalbankapi.exception;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(Long accountId) {
        super("Saldo insuficiente na conta: id=" + accountId);
    }
}
