package app.notificationservice.web.mapper;
import app.notificationservice.entity.NotificationPreference;
import app.notificationservice.web.DTOs.NotificationPreferenceResponse;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
public class NotificationPreferenceMapper {

    public static NotificationPreferenceResponse toNotificationPreferenceResponse(NotificationPreference notificationPreference) {

        return NotificationPreferenceResponse.builder()
                .contactInfo(notificationPreference.getContactInfo())
                .enabled(notificationPreference.isEnabled())
                .type(notificationPreference.getType())
                .build();
    }
}
