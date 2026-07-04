package com.example.digitalbankapi.service;

import com.example.digitalbankapi.dto.CreateAccountRequest;
import com.example.digitalbankapi.entity.Account;
import com.example.digitalbankapi.entity.AccountMovement;
import com.example.digitalbankapi.exception.AccountNotFoundException;
import com.example.digitalbankapi.repository.AccountMovementRepository;
import com.example.digitalbankapi.repository.AccountRepository;
import com.example.digitalbankapi.util.Money;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMovementRepository accountMovementRepository;

    @Transactional
    public Account create(CreateAccountRequest request) {
        Account account = accountRepository.save(
                new Account(request.name(), Money.normalize(request.initialBalance())));
        log.info("Conta criada: id={}, nome={}", account.getId(), account.getName());
        return account;
    }

    @Transactional(readOnly = true)
    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Account getById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<AccountMovement> getMovements(Long accountId) {
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        return accountMovementRepository.findByAccountIdOrderByCreatedAtDescIdDesc(accountId);
    }
}
