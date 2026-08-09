package app.notificationservice.service;
import app.notificationservice.entity.Notification;
import app.notificationservice.entity.NotificationPreference;
import app.notificationservice.entity.NotificationStatus;
import app.notificationservice.repository.NotificationRepository;
import app.notificationservice.web.DTOs.NotificationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationUTest  {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationPreferenceService notificationPreferenceService;
    @Mock
    private MailSender mailSender;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void retryFailed_preferenceDisabled_thenExceptionIsThrown(){
        UUID id = UUID.randomUUID();
        NotificationPreference enabledPreference = NotificationPreference.builder()
                .enabled(false)
                .build();

        when(notificationPreferenceService.getByUserId(id)).thenReturn(enabledPreference);

        assertThrows(IllegalArgumentException.class,() -> notificationService.retryFailed(id));
    }

    @Test
    void retryFailed_preferenceEnabled_weHave2Failed_(){
        UUID id = UUID.randomUUID();
        NotificationPreference enabledPreference = NotificationPreference.builder()
                .enabled(true)
                .build();

        when(notificationPreferenceService.getByUserId(id)).thenReturn(enabledPreference);

        List<Notification> failedEmails = new ArrayList<>();
        Notification failedNotification1 = Notification.builder()
                .deleted(false)
                .status(NotificationStatus.FAILED)
                .build();

        Notification failedNotification2 = Notification.builder()
                .deleted(false)
                .status(NotificationStatus.FAILED)
                .build();

        Notification failedNotification3 = Notification.builder()
                .deleted(true)
                .status(NotificationStatus.FAILED)
                .build();

        failedEmails.add(failedNotification1);
        failedEmails.add(failedNotification2);
        failedEmails.add(failedNotification3);
        when(notificationRepository.findByUserId(id)).thenReturn(failedEmails);

       notificationService.retryFailed(id);
       verify(mailSender,times(2)).send(any(SimpleMailMessage.class));
       assertEquals(NotificationStatus.SUCCEEDED, failedNotification1.getStatus());
       assertEquals(NotificationStatus.SUCCEEDED, failedNotification2.getStatus());
       assertEquals(NotificationStatus.FAILED, failedNotification3.getStatus());
    }

    @Test
    void send_whenPreferenceDisabled_throws() {
        UUID userId = UUID.randomUUID();
        NotificationRequest request = NotificationRequest.builder().userId(userId).subject("S").body("B").build();
        when(notificationPreferenceService.getByUserId(userId))
                .thenReturn(NotificationPreference.builder().enabled(false).build());

        assertThrows(IllegalStateException.class, () -> notificationService.send(request));
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void send_whenEnabled_savesSucceeded() {
        UUID userId = UUID.randomUUID();
        NotificationRequest request = NotificationRequest.builder().userId(userId).subject("S").body("B").build();
        when(notificationPreferenceService.getByUserId(userId))
                .thenReturn(NotificationPreference.builder().enabled(true).contactInfo("a@mail.com").build());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        Notification result = notificationService.send(request);

        assertEquals(NotificationStatus.SUCCEEDED, result.getStatus());
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void send_whenMailFails_savesFailed() {
        UUID userId = UUID.randomUUID();
        NotificationRequest request = NotificationRequest.builder().userId(userId).subject("S").body("B").build();
        when(notificationPreferenceService.getByUserId(userId))
                .thenReturn(NotificationPreference.builder().enabled(true).contactInfo("a@mail.com").build());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("smtp down")).when(mailSender).send(any(SimpleMailMessage.class));

        Notification result = notificationService.send(request);

        assertEquals(NotificationStatus.FAILED, result.getStatus());
    }

    @Test
    void getHistory_returnsOnlyNotDeleted() {
        UUID userId = UUID.randomUUID();
        Notification kept = Notification.builder().deleted(false).build();
        Notification gone = Notification.builder().deleted(true).build();
        when(notificationRepository.findByUserId(userId)).thenReturn(List.of(kept, gone));

        List<Notification> result = notificationService.getHistory(userId);

        assertEquals(1, result.size());
        assertSame(kept, result.get(0));
    }

    @Test
    void deleteHistory_marksNotDeletedAsDeleted() {
        UUID userId = UUID.randomUUID();
        Notification n = Notification.builder().deleted(false).build();
        when(notificationRepository.findByUserId(userId)).thenReturn(List.of(n));

        notificationService.deleteHistory(userId);

        assertTrue(n.isDeleted());
        verify(notificationRepository).save(n);
    }
}
