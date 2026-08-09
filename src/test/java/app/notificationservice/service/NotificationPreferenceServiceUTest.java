package app.notificationservice.service;

import app.notificationservice.entity.NotificationPreference;
import app.notificationservice.repository.NotificationPreferenceRepository;
import app.notificationservice.web.DTOs.NotificationPreferenceRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationPreferenceServiceUTest {

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @InjectMocks
    private NotificationPreferenceService notificationPreferenceService;

    @Test
    void upsert_whenExists_updatesIt() {
        UUID userId = UUID.randomUUID();
        NotificationPreferenceRequest request = NotificationPreferenceRequest.builder()
                .userId(userId).notificationEnabled(true).contactInfo("new@mail.com").build();
        NotificationPreference existing = NotificationPreference.builder()
                .userId(userId).enabled(false).contactInfo("old@mail.com").build();

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(preferenceRepository.save(existing)).thenReturn(existing);

        NotificationPreference result = notificationPreferenceService.upsert(request);

        assertTrue(existing.isEnabled());
        assertEquals("new@mail.com", existing.getContactInfo());
        assertEquals(existing, result);
        verify(preferenceRepository).save(existing);
    }

    @Test
    void upsert_whenNotExists_createsIt() {
        UUID userId = UUID.randomUUID();
        NotificationPreferenceRequest request = NotificationPreferenceRequest.builder()
                .userId(userId).notificationEnabled(true).contactInfo("a@mail.com").build();

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(preferenceRepository.save(any(NotificationPreference.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationPreference result = notificationPreferenceService.upsert(request);

        assertEquals(userId, result.getUserId());
        assertTrue(result.isEnabled());
        assertEquals("a@mail.com", result.getContactInfo());
        verify(preferenceRepository).save(any(NotificationPreference.class));
    }

    @Test
    void getByUserId_whenExists_returnsIt() {
        UUID userId = UUID.randomUUID();
        NotificationPreference pref = NotificationPreference.builder().userId(userId).build();
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(pref));

        assertEquals(pref, notificationPreferenceService.getByUserId(userId));
    }

    @Test
    void getByUserId_whenMissing_throws() {
        UUID userId = UUID.randomUUID();
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> notificationPreferenceService.getByUserId(userId));
    }
}
