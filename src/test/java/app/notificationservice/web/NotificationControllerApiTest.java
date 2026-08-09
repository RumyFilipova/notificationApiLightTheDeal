package app.notificationservice.web;
import app.notificationservice.config.ApiKeyAuthenticationFilter;
import app.notificationservice.entity.Notification;
import app.notificationservice.entity.NotificationPreference;
import app.notificationservice.entity.NotificationStatus;
import app.notificationservice.entity.NotificationType;
import app.notificationservice.service.NotificationPreferenceService;
import app.notificationservice.service.NotificationService;
import app.notificationservice.web.DTOs.NotificationRequest;
import app.notificationservice.web.controllers.NotificationController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NotificationControllerApiTest {

    @MockitoBean
    private NotificationService notificationService;
    @MockitoBean
    private NotificationPreferenceService notificationPreferenceService;
    @MockitoBean
    private ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    @Autowired
    MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getPreferencesEndpoint_statusOk200_invokeSendMethod() throws Exception {
        UUID userId = UUID.randomUUID();
        NotificationPreference preference = NotificationPreference.builder()
                .userId(userId)
                .type(NotificationType.EMAIL)
                .enabled(true)
                .contactInfo("client@mail.bg")
                .build();

        when(notificationPreferenceService.getByUserId(userId)).thenReturn(preference);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/api/V1/notifications/preferences")
                .param("userId",userId.toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.contactInfo").value("client@mail.bg"))
                .andExpect(jsonPath("$.type").value("EMAIL"));
    }
    @Test
    void getHistoryEndpoint_statusOk200_invokeGetHistoryMethod() throws Exception {
        UUID userId = UUID.randomUUID();

        Notification notification1 = Notification.builder()
                .subject("new income")
                .createdOn(LocalDateTime.now())
                .status(NotificationStatus.SUCCEEDED)
                .type(NotificationType.EMAIL)
                .build();
        Notification notification2 = Notification.builder()
                .subject("new income")
                .createdOn(LocalDateTime.now())
                .status(NotificationStatus.FAILED)
                .type(NotificationType.EMAIL)
                .build();
       when(notificationService.getHistory(userId)).thenReturn(List.of(notification1,notification2));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/api/V1/notifications")
                .param("userId",userId.toString());
        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(2)))
                .andExpect(jsonPath("$[0].subject").value("new income"))
                .andExpect(jsonPath("$[0].createdOn").exists())
                .andExpect(jsonPath("$[1].status").value("FAILED"));
    }

    @Test
    void postSendNotification_status201_withJsonResponse() throws Exception {
        UUID userId = UUID.randomUUID();

        NotificationRequest notificationRequest = NotificationRequest.builder()
                .userId(userId)
                .type(NotificationType.EMAIL)
                .subject("New transfer")
                .body("New transfer has been done")
                .build();

        Notification notificationSaved = Notification.builder()
                .subject("New transfer")
                .createdOn(LocalDateTime.now())
                .status(NotificationStatus.SUCCEEDED)
                .type(NotificationType.EMAIL)
                .userId(userId)
                .build();

        when(notificationService.send(any())).thenReturn(notificationSaved);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/V1/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notificationRequest));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subject").value("New transfer"))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.type").value("EMAIL"));
    }

    @Test
    void putRetryFailedNotification_return202_invokesService() throws Exception {
        UUID userId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = put("/api/V1/notifications")
                .param("userId",userId.toString());

        mockMvc.perform(request)
                .andExpect(status().isAccepted());
        verify(notificationService).retryFailed(userId);
    }

    @Test
    void deleteHistory_status202_invokeDeleteNotification() throws Exception {
        UUID userId = UUID.randomUUID();

        MockHttpServletRequestBuilder request = delete("/api/V1/notifications")
                .param("userId",userId.toString());
        mockMvc.perform(request)
                .andExpect(status().isAccepted());
        verify(notificationService).deleteHistory(userId);
    }
}
