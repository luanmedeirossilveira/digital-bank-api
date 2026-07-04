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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private AccountMovementRepository accountMovementRepository;

    @Mock
    private TransferAuditService transferAuditService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TransferService transferService;

    private Account account(Long id, String balance) {
        Account account = new Account("Conta " + id, new BigDecimal(balance));
        account.setId(id);
        return account;
    }

    @Test
    void shouldTransferAmountBetweenAccounts() {
        // Arrange
        Account source = account(1L, "100.00");
        Account destination = account(2L, "50.00");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destination));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Transfer result = transferService.transfer(new TransferRequest(1L, 2L, new BigDecimal("30.5")));

        // Assert
        assertThat(source.getBalance()).isEqualByComparingTo("69.50");
        assertThat(destination.getBalance()).isEqualByComparingTo("80.50");
        assertThat(result.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(result.getAmount()).isEqualByComparingTo("30.50");
        assertThat(result.getSourceAccount()).isSameAs(source);
        assertThat(result.getDestinationAccount()).isSameAs(destination);
    }

    @Test
    void shouldRecordDebitAndCreditMovements() {
        // Arrange
        Account source = account(1L, "100.00");
        Account destination = account(2L, "50.00");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destination));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Transfer transfer = transferService.transfer(new TransferRequest(1L, 2L, new BigDecimal("30.00")));

        // Assert
        ArgumentCaptor<AccountMovement> captor = ArgumentCaptor.forClass(AccountMovement.class);
        verify(accountMovementRepository, times(2)).save(captor.capture());
        List<AccountMovement> movements = captor.getAllValues();
        assertThat(movements).extracting(AccountMovement::getType)
                .containsExactly(MovementType.DEBIT, MovementType.CREDIT);
        assertThat(movements.get(0).getAccount()).isSameAs(source);
        assertThat(movements.get(1).getAccount()).isSameAs(destination);
        assertThat(movements).allSatisfy(movement -> {
            assertThat(movement.getAmount()).isEqualByComparingTo("30.00");
            assertThat(movement.getTransfer()).isSameAs(transfer);
        });
    }

    @Test
    void shouldPublishCompletedEventAfterSuccessfulTransfer() {
        // Arrange
        Account source = account(1L, "100.00");
        Account destination = account(2L, "50.00");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destination));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        transferService.transfer(new TransferRequest(1L, 2L, new BigDecimal("30.00")));

        // Assert
        ArgumentCaptor<TransferCompletedEvent> captor = ArgumentCaptor.forClass(TransferCompletedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().sourceAccountId()).isEqualTo(1L);
        assertThat(captor.getValue().destinationAccountId()).isEqualTo(2L);
        assertThat(captor.getValue().amount()).isEqualByComparingTo("30.00");
    }

    @Test
    void shouldLockAccountsInAscendingIdOrderToAvoidDeadlock() {
        // Arrange
        Account source = account(5L, "100.00");
        Account destination = account(2L, "50.00");
        when(accountRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destination));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        transferService.transfer(new TransferRequest(5L, 2L, new BigDecimal("10.00")));

        // Assert
        InOrder lockOrder = inOrder(accountRepository);
        lockOrder.verify(accountRepository).findByIdForUpdate(2L);
        lockOrder.verify(accountRepository).findByIdForUpdate(5L);
    }

    @Test
    void shouldAllowTransferringTheEntireBalance() {
        // Arrange
        Account source = account(1L, "100.00");
        Account destination = account(2L, "50.00");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destination));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        transferService.transfer(new TransferRequest(1L, 2L, new BigDecimal("100.00")));

        // Assert
        assertThat(source.getBalance()).isEqualByComparingTo("0.00");
        assertThat(destination.getBalance()).isEqualByComparingTo("150.00");
    }

    @Test
    void shouldNormalizeAmountToMoneyScaleBeforeApplying() {
        // Arrange
        Account source = account(1L, "100.00");
        Account destination = account(2L, "50.00");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destination));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Transfer result = transferService.transfer(new TransferRequest(1L, 2L, new BigDecimal("10.005")));

        // Assert
        assertThat(result.getAmount()).isEqualTo(new BigDecimal("10.01"));
        assertThat(source.getBalance()).isEqualByComparingTo("89.99");
        assertThat(destination.getBalance()).isEqualByComparingTo("60.01");
    }

    @Test
    void shouldFailWhenBalanceIsInsufficient() {
        // Arrange
        Account source = account(1L, "10.00");
        Account destination = account(2L, "50.00");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destination));

        // Act / Assert
        assertThatThrownBy(() -> transferService.transfer(new TransferRequest(1L, 2L, new BigDecimal("50.00"))))
                .isInstanceOf(InsufficientBalanceException.class);
        assertThat(source.getBalance()).isEqualByComparingTo("10.00");
        assertThat(destination.getBalance()).isEqualByComparingTo("50.00");
        verifyNoInteractions(transferRepository, accountMovementRepository, eventPublisher);
    }

    @Test
    void shouldRecordFailedTransferForAuditWhenBalanceIsInsufficient() {
        // Arrange
        Account source = account(1L, "10.00");
        Account destination = account(2L, "50.00");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destination));

        // Act / Assert
        assertThatThrownBy(() -> transferService.transfer(new TransferRequest(1L, 2L, new BigDecimal("50.00"))))
                .isInstanceOf(InsufficientBalanceException.class);
        verify(transferAuditService)
                .recordFailedTransfer(eq(1L), eq(2L), any(BigDecimal.class), eq("Saldo insuficiente"));
    }

    @Test
    void shouldFailWhenSourceAccountDoesNotExist() {
        // Arrange
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> transferService.transfer(new TransferRequest(1L, 2L, new BigDecimal("10.00"))))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("id=1");
        verifyNoInteractions(transferRepository, accountMovementRepository, transferAuditService, eventPublisher);
    }

    @Test
    void shouldFailWhenDestinationAccountDoesNotExist() {
        // Arrange
        Account source = account(1L, "100.00");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> transferService.transfer(new TransferRequest(1L, 2L, new BigDecimal("10.00"))))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("id=2");
        verifyNoInteractions(transferRepository, accountMovementRepository, transferAuditService, eventPublisher);
    }

    @Test
    void shouldFailWhenSourceAndDestinationAreTheSameAccount() {
        // Act / Assert
        assertThatThrownBy(() -> transferService.transfer(new TransferRequest(1L, 1L, new BigDecimal("10.00"))))
                .isInstanceOf(InvalidTransferException.class);
        verifyNoInteractions(accountRepository, transferRepository, accountMovementRepository,
                transferAuditService, eventPublisher);
    }

    @Test
    void shouldNotPublishNotificationEventWhenTransferFails() {
        // Arrange
        Account source = account(1L, "10.00");
        Account destination = account(2L, "50.00");
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destination));

        // Act / Assert
        assertThatThrownBy(() -> transferService.transfer(new TransferRequest(1L, 2L, new BigDecimal("999.00"))))
                .isInstanceOf(InsufficientBalanceException.class);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldFailWhenAmountIsZeroOrNegative() {
        // Act / Assert
        assertThatThrownBy(() -> transferService.transfer(new TransferRequest(1L, 2L, BigDecimal.ZERO)))
                .isInstanceOf(InvalidTransferException.class);
        assertThatThrownBy(() -> transferService.transfer(new TransferRequest(1L, 2L, new BigDecimal("-10.00"))))
                .isInstanceOf(InvalidTransferException.class);
        verifyNoInteractions(accountRepository, transferRepository, accountMovementRepository,
                transferAuditService, eventPublisher);
    }
}
