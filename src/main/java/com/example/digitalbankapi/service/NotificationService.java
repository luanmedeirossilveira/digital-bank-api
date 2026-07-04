package com.example.digitalbankapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.example.digitalbankapi.util.Money;

import java.math.BigDecimal;

@Service
@Slf4j
public class NotificationService {

    /**
     * Simulação de envio de notificação ao cliente após transferência concluída com sucesso.
     * Em um cenário real, poderia integrar com e-mail, SMS, push ou mensageria.
     */
    public void notifyTransferCompleted(Long sourceAccountId,
                                        Long destinationAccountId, BigDecimal amount) {
        String convertedAmount = Money.format(amount);
        log.info("[NOTIFICAÇÃO] Transferência realizada com sucesso! Conta {} realizou transferência para conta {} no valor de {}.",
                sourceAccountId, destinationAccountId, convertedAmount);
    }
}
