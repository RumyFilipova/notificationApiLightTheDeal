package app.notificationservice.web.DTOs;

import app.notificationservice.entity.NotificationStatus;
import app.notificationservice.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor

public class NotificationResponse {

    private String subject;
    private LocalDateTime createdOn;
    private NotificationStatus status;
    private NotificationType type;
}
