package app.notificationservice.web.mapper;

import app.notificationservice.entity.Notification;
import app.notificationservice.entity.NotificationPreference;
import app.notificationservice.entity.NotificationType;
import app.notificationservice.web.DTOs.NotificationPreferenceResponse;
import app.notificationservice.web.DTOs.NotificationResponse;
import lombok.experimental.UtilityClass;

@UtilityClass
public class DtoMapper {

    public static NotificationPreferenceResponse from(NotificationPreference preference) {

return NotificationPreferenceResponse.builder()
        .type(preference.getType())
        .contactInfo(preference.getContactInfo())
        .enabled(preference.isEnabled())
        .build();
    }

    public static NotificationResponse from (Notification notification) {

        return NotificationResponse.builder()
                .subject(notification.getSubject())
                .createdOn(notification.getCreatedOn())
                .status(notification.getStatus())
                .type(notification.getType())
                .build();
    }
}
