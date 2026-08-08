package app.notificationservice.web.mapper;

import app.notificationservice.entity.Notification;
import app.notificationservice.web.DTOs.NotificationResponse;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class NotificationMapper {

    public static NotificationResponse toNotificationResponse(Notification notification) {

        return NotificationResponse.builder()
                .type(notification.getType())
                .subject(notification.getSubject())
                .status(notification.getStatus())
                .createdOn(notification.getCreatedOn())
                .build();
    }
}
