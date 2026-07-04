package com.example.digitalbankapi.entity;

public enum TransferStatus {

    /** Transferência validada e em execução dentro da transação. */
    PENDING,

    /** Transferência efetivada: saldos ajustados e movimentações registradas. */
    COMPLETED,

    /** Tentativa registrada para auditoria após falha de negócio (ex.: saldo insuficiente). */
    FAILED
}
