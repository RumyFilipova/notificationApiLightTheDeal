package app.notificationservice.service;
import app.notificationservice.entity.Notification;
import app.notificationservice.entity.NotificationPreference;
import app.notificationservice.entity.NotificationStatus;
import app.notificationservice.repository.NotificationRepository;
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
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
}
