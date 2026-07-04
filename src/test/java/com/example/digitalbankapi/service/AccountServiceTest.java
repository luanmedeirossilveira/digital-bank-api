package com.example.digitalbankapi.service;

import com.example.digitalbankapi.dto.CreateAccountRequest;
import com.example.digitalbankapi.entity.Account;
import com.example.digitalbankapi.entity.AccountMovement;
import com.example.digitalbankapi.entity.MovementType;
import com.example.digitalbankapi.exception.AccountNotFoundException;
import com.example.digitalbankapi.repository.AccountMovementRepository;
import com.example.digitalbankapi.repository.AccountRepository;
import com.example.digitalbankapi.util.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMovementRepository accountMovementRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldCreateAccountWithNormalizedBalanceScale() {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest("Maria Silva", new BigDecimal("1000.5"));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Account created = accountService.create(request);

        // Assert
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Maria Silva");
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo("1000.50");
        assertThat(captor.getValue().getBalance().scale()).isEqualTo(Money.SCALE);
        assertThat(created.getName()).isEqualTo("Maria Silva");
    }

    @Test
    void shouldReturnAccountWhenIdExists() {
        // Arrange
        Account account = new Account("João Souza", new BigDecimal("500.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        // Act
        Account found = accountService.getById(1L);

        // Assert
        assertThat(found).isSameAs(account);
    }

    @Test
    void shouldThrowNotFoundWhenAccountDoesNotExist() {
        // Arrange
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        // Act / Assert
        assertThatThrownBy(() -> accountService.getById(99L))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessageContaining("id=99");
    }

    @Test
    void shouldReturnMovementsOfExistingAccount() {
        // Arrange
        Account account = new Account("Maria Silva", new BigDecimal("1000.00"));
        AccountMovement movement = new AccountMovement(account, MovementType.DEBIT, new BigDecimal("30.00"), null);
        when(accountRepository.existsById(1L)).thenReturn(true);
        when(accountMovementRepository.findByAccountIdOrderByCreatedAtDescIdDesc(1L)).thenReturn(List.of(movement));

        // Act
        List<AccountMovement> result = accountService.getMovements(1L);

        // Assert
        assertThat(result).containsExactly(movement);
    }

    @Test
    void shouldThrowNotFoundWhenListingMovementsOfMissingAccount() {
        // Arrange
        when(accountRepository.existsById(99L)).thenReturn(false);

        // Act / Assert
        assertThatThrownBy(() -> accountService.getMovements(99L))
                .isInstanceOf(AccountNotFoundException.class);
        verifyNoInteractions(accountMovementRepository);
    }

    @Test
    void shouldListAllAccounts() {
        // Arrange
        List<Account> accounts = List.of(
                new Account("Maria Silva", new BigDecimal("1000.00")),
                new Account("João Souza", new BigDecimal("500.00")));
        when(accountRepository.findAll()).thenReturn(accounts);

        // Act
        List<Account> result = accountService.findAll();

        // Assert
        assertThat(result).hasSize(2).containsExactlyElementsOf(accounts);
    }
}
