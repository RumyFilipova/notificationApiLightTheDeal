package app.notificationservice.web.DTOs;

import app.notificationservice.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class NotificationPreferenceResponse {
    private NotificationType type;
    private boolean enabled;
    private String contactInfo;

}
