package com.example.digitalbankapi.service;

import com.example.digitalbankapi.dto.TransferRequest;
import com.example.digitalbankapi.entity.Account;
import com.example.digitalbankapi.entity.AccountMovement;
import com.example.digitalbankapi.entity.MovementType;
import com.example.digitalbankapi.entity.Transfer;
import com.example.digitalbankapi.entity.TransferStatus;
import com.example.digitalbankapi.event.TransferCompletedEvent;
import com.example.digitalbankapi.exception.AccountNotFoundException;
import com.example.digitalbankapi.exception.InsufficientBalanceException;
import com.example.digitalbankapi.exception.InvalidTransferException;
import com.example.digitalbankapi.repository.AccountMovementRepository;
import com.example.digitalbankapi.repository.AccountRepository;
import com.example.digitalbankapi.repository.TransferRepository;
import com.example.digitalbankapi.util.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final AccountMovementRepository accountMovementRepository;
    private final TransferAuditService transferAuditService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Transfer transfer(TransferRequest request) {
        BigDecimal amount = Money.normalize(request.amount());
        if (amount.signum() <= 0) {
            throw new InvalidTransferException("Valor da transferência deve ser maior que zero");
        }
        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new InvalidTransferException("Conta de origem e destino devem ser diferentes");
        }

        Account source;
        Account destination;
        // locks sempre em ordem crescente de id para evitar deadlock entre transferências cruzadas
        if (request.sourceAccountId() < request.destinationAccountId()) {
            source = lockAccount(request.sourceAccountId());
            destination = lockAccount(request.destinationAccountId());
        } else {
            destination = lockAccount(request.destinationAccountId());
            source = lockAccount(request.sourceAccountId());
        }

        if (source.getBalance().compareTo(amount) < 0) {
            // registra a tentativa como FAILED em transação separada, pois a transação atual sofrerá rollback
            transferAuditService.recordFailedTransfer(
                    source.getId(), destination.getId(), amount, "Saldo insuficiente");
            throw new InsufficientBalanceException(source.getId());
        }

        // nasce PENDING (validada, em execução) e passa a COMPLETED ao concluir os ajustes de saldo
        Transfer transfer = transferRepository.save(
                new Transfer(source, destination, amount, TransferStatus.PENDING));

        source.setBalance(source.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));

        accountMovementRepository.save(new AccountMovement(source, MovementType.DEBIT, amount, transfer));
        accountMovementRepository.save(new AccountMovement(destination, MovementType.CREDIT, amount, transfer));
        transfer.setStatus(TransferStatus.COMPLETED);

        eventPublisher.publishEvent(
                new TransferCompletedEvent(transfer.getId(), source.getId(), destination.getId(), amount));

        log.info("Transferência concluída: id={}, origem={}, destino={}, valor={}",
                transfer.getId(), source.getId(), destination.getId(), amount);
        return transfer;
    }

    private Account lockAccount(Long id) {
        return accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }
}
