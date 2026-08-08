package app.notificationservice.web.controllers;

import app.notificationservice.entity.Notification;
import app.notificationservice.entity.NotificationPreference;
import app.notificationservice.service.NotificationPreferenceService;
import app.notificationservice.service.NotificationService;
import app.notificationservice.web.DTOs.NotificationPreferenceResponse;
import app.notificationservice.web.DTOs.NotificationRequest;
import app.notificationservice.web.DTOs.NotificationResponse;
import app.notificationservice.web.mapper.DtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/V1/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final NotificationPreferenceService notificationPreferenceService;

    @Autowired
    public NotificationController(NotificationService notificationService, NotificationPreferenceService notificationPreferenceService) {
        this.notificationService = notificationService;
        this.notificationPreferenceService = notificationPreferenceService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(
            @RequestBody NotificationRequest request
    ) {

        Notification notification = notificationService.send(request);

        return ResponseEntity
                .status(HttpStatus.CREATED).body(DtoMapper.from(notification));
    }

    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreferenceResponse> getPreferenceForUser(@RequestParam("userId") UUID userId) {

        NotificationPreference preference = notificationPreferenceService.getByUserId(userId);

        return ResponseEntity.ok(DtoMapper.from(preference));
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getHistory(@RequestParam("userId") UUID userId) {

        List<Notification> notifications = notificationService.getHistory(userId);
        List<NotificationResponse> responses = notifications.stream().map(DtoMapper::from).toList();

        return ResponseEntity.ok(responses);
    }

    @PutMapping
    public ResponseEntity<Void> retryFailedNotification(@RequestParam UUID userId) {
        notificationService.retryFailed(userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteNotification(@RequestParam UUID userId) {

        notificationService.deleteHistory(userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
