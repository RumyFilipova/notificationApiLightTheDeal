package app.notificationservice.web.DTOs;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor

public class NotificationPreferenceRequest {

    private UUID userId;
    private boolean notificationEnabled;
    private String contactInfo;


}
