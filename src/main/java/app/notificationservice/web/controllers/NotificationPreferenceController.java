package app.notificationservice.web.controllers;

import app.notificationservice.entity.NotificationPreference;
import app.notificationservice.service.NotificationPreferenceService;
import app.notificationservice.web.DTOs.NotificationPreferenceRequest;
import app.notificationservice.web.DTOs.NotificationPreferenceResponse;
import app.notificationservice.web.mapper.DtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/V1/notification-preferences")
public class NotificationPreferenceController {

    private final NotificationPreferenceService notificationPreferenceService;

    public NotificationPreferenceController(NotificationPreferenceService notificationPreferenceService) {
        this.notificationPreferenceService = notificationPreferenceService;
    }

    @PostMapping
    public ResponseEntity<NotificationPreferenceResponse> upsertPreference(
            @RequestBody NotificationPreferenceRequest request
    ) {
        // If the service is different from Email, if this is valid option
        NotificationPreference preference = notificationPreferenceService.upsert(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.from(preference));
    }

    @GetMapping
    public ResponseEntity<NotificationPreferenceResponse> getPreferenceForUser(@RequestParam("userId") UUID userId) {
        NotificationPreference preference = notificationPreferenceService.getByUserId(userId);
        return ResponseEntity.ok(DtoMapper.from(preference));
    }
}
