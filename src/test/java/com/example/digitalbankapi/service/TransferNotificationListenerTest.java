package com.example.digitalbankapi.service;

import com.example.digitalbankapi.event.TransferCompletedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransferNotificationListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TransferNotificationListener listener;

    @Test
    void shouldNotifyClientWhenTransferCompletes() {
        // Arrange
        TransferCompletedEvent event = new TransferCompletedEvent(10L, 1L, 2L, new BigDecimal("30.50"));

        // Act
        listener.onTransferCompleted(event);

        // Assert
        verify(notificationService).notifyTransferCompleted(1L, 2L, new BigDecimal("30.50"));
    }
}
