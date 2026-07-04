package com.example.digitalbankapi.service;

import com.example.digitalbankapi.entity.Account;
import com.example.digitalbankapi.entity.Transfer;
import com.example.digitalbankapi.entity.TransferStatus;
import com.example.digitalbankapi.repository.AccountRepository;
import com.example.digitalbankapi.repository.TransferRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferAuditServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransferAuditService transferAuditService;

    private Account account(Long id) {
        Account account = new Account("Conta " + id, new BigDecimal("0.00"));
        account.setId(id);
        return account;
    }

    @Test
    void shouldPersistFailedTransferWithReason() {
        // Arrange
        when(accountRepository.getReferenceById(1L)).thenReturn(account(1L));
        when(accountRepository.getReferenceById(2L)).thenReturn(account(2L));

        // Act
        transferAuditService.recordFailedTransfer(1L, 2L, new BigDecimal("50.00"), "Saldo insuficiente");

        // Assert
        ArgumentCaptor<Transfer> captor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(captor.capture());
        Transfer saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(TransferStatus.FAILED);
        assertThat(saved.getFailureReason()).isEqualTo("Saldo insuficiente");
        assertThat(saved.getSourceAccount().getId()).isEqualTo(1L);
        assertThat(saved.getDestinationAccount().getId()).isEqualTo(2L);
        assertThat(saved.getAmount()).isEqualByComparingTo("50.00");
    }
}
