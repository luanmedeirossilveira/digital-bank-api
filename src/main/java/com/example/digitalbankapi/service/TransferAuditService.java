package com.example.digitalbankapi.service;

import com.example.digitalbankapi.entity.Account;
import com.example.digitalbankapi.entity.Transfer;
import com.example.digitalbankapi.entity.TransferStatus;
import com.example.digitalbankapi.repository.AccountRepository;
import com.example.digitalbankapi.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Registra tentativas de transferência que falharam por regra de negócio.
 * Roda em transação própria (REQUIRES_NEW) para que o registro FAILED sobreviva
 * ao rollback da transação da transferência.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransferAuditService {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedTransfer(Long sourceAccountId, Long destinationAccountId,
                                     BigDecimal amount, String reason) {
        Account source = accountRepository.getReferenceById(sourceAccountId);
        Account destination = accountRepository.getReferenceById(destinationAccountId);

        Transfer failed = new Transfer(source, destination, amount, TransferStatus.FAILED);
        failed.setFailureReason(reason);
        transferRepository.save(failed);

        log.warn("Transferência falhou e foi registrada para auditoria: origem={}, destino={}, valor={}, motivo={}",
                sourceAccountId, destinationAccountId, amount, reason);
    }
}
