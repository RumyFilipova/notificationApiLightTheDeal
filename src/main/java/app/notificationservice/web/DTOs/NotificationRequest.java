package app.notificationservice.web.DTOs;
import app.notificationservice.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor

public class NotificationRequest {

    private UUID userId;
    private NotificationType type;
    private String subject;
    private String body;

}
