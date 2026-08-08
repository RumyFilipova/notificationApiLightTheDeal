package app.notificationservice.service;
import app.notificationservice.entity.Notification;
import app.notificationservice.entity.NotificationPreference;
import app.notificationservice.entity.NotificationStatus;
import app.notificationservice.entity.NotificationType;
import app.notificationservice.repository.NotificationPreferenceRepository;
import app.notificationservice.repository.NotificationRepository;
import app.notificationservice.web.mapper.DtoMapper;
import app.notificationservice.web.DTOs.NotificationRequest;
import app.notificationservice.web.DTOs.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceService notificationPreferenceService;
    private final MailSender mailSender;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, NotificationPreferenceRepository notificationPreferenceRepository, NotificationPreferenceService notificationPreferenceService, MailSender mailSender) {
        this.notificationRepository = notificationRepository;
        this.notificationPreferenceService = notificationPreferenceService;
        this.mailSender = mailSender;
    }


    public Notification send(NotificationRequest request) {

        NotificationPreference preference = notificationPreferenceService.getByUserId(request.getUserId());

        boolean enabled = preference.isEnabled();

        if (!enabled) {
            throw new IllegalStateException("Notification preference is not enabled");
        }

        Notification notification = Notification.builder()
                .subject(request.getSubject())
                .body(request.getBody())
                .createdOn(LocalDateTime.now())
                .type(NotificationType.EMAIL)
                .userId(request.getUserId())
                .deleted(false)
                .build();

        //SENT EMAIL

        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setTo(preference.getContactInfo());
        mailMessage.setSubject(request.getSubject());
        mailMessage.setText(request.getBody());

        try {
            mailSender.send(mailMessage);
            notification.setStatus(NotificationStatus.SUCCEEDED);
        } catch (Exception e) {
            log.error("Mail send failed due to: {}", e.getMessage());
            notification.setStatus(NotificationStatus.FAILED);
        }

        return notificationRepository.save(notification);
    }


    private void sendEmail(SimpleMailMessage mailMessage, Notification notification) {

    }

    public List<Notification> getHistory(UUID userId) {

        return notificationRepository.findByUserId(userId)
                .stream()
                .filter(n -> !n.isDeleted())
                .toList();
    }

    public void retryFailed(UUID userId) {

        NotificationPreference notificationPreference = notificationPreferenceService.getByUserId(userId);

        if (!notificationPreference.isEnabled()) {
            throw new IllegalArgumentException("User with id [%s] has disabled their notifications.".formatted(userId));
        }

        List<Notification> failedNotifications = getNotDeletedNotifications(userId)
                .stream()
                .filter(n -> n.getStatus() == NotificationStatus.FAILED)
                .toList();

        failedNotifications.forEach(n -> {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(notificationPreference.getContactInfo());
            mailMessage.setSubject(n.getSubject());
            mailMessage.setText(n.getBody());

            sendEmail(mailMessage, n);
            notificationRepository.save(n);
        });
    }

    private List<Notification> getNotDeletedNotifications(UUID userId) {
        return notificationRepository.findByUserId(userId)
                .stream()
                .filter(n -> !n.isDeleted())
                .toList();
    }

    public void deleteHistory(UUID userId) {

        getNotDeletedNotifications(userId)
                .forEach(n -> {
                    n.setDeleted(true);
                    notificationRepository.save(n);
                });
    }


}