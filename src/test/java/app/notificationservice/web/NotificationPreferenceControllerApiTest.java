package app.notificationservice.web;

import app.notificationservice.config.ApiKeyAuthenticationFilter;
import app.notificationservice.entity.NotificationPreference;
import app.notificationservice.entity.NotificationType;
import app.notificationservice.service.NotificationPreferenceService;
import app.notificationservice.web.DTOs.NotificationPreferenceRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import app.notificationservice.web.controllers.NotificationPreferenceController;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationPreferenceController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NotificationPreferenceControllerApiTest {

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

        MockHttpServletRequestBuilder request = get("/api/V1/notification-preferences")
                .param("userId",userId.toString());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("EMAIL"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.contactInfo").isNotEmpty());
    }

    @Test
    void postUpsertPreference_statusOk_invokeUpsertMethod() throws Exception {

        UUID userId = UUID.randomUUID();
           NotificationPreference preference = NotificationPreference.builder()
                .userId(userId)
                .type(NotificationType.EMAIL)
                .enabled(false)
                .contactInfo("client@mail.bg")
                .build();

        NotificationPreferenceRequest preferenceRequest =NotificationPreferenceRequest.builder()
                .userId(userId)
                .notificationEnabled(false)
                .contactInfo("client@mail.bg")
                .build();

        when(notificationPreferenceService.upsert(any())).thenReturn(preference);

        MockHttpServletRequestBuilder request = post("/api/V1/notification-preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(preferenceRequest));

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.enabled").value(false));
    }
}
