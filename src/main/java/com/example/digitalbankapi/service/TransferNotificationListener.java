package com.example.digitalbankapi.service;

import com.example.digitalbankapi.event.TransferCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TransferNotificationListener {

    private final NotificationService notificationService;

    // AFTER_COMMIT garante notificação somente após a transferência ser efetivada no banco
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransferCompleted(TransferCompletedEvent event) {
        notificationService.notifyTransferCompleted(event.sourceAccountId(), event.destinationAccountId(), event.amount());
    }
}
