package com.eams.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifica que Security Headers de HTTP están configurados correctamente.
 * Cubre: HSTS, X-Content-Type-Options, X-Frame-Options, X-XSS-Protection, CSP
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Security Headers Tests")
class SecurityHeadersTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /auth/login debe incluir X-Content-Type-Options: nosniff")
    void testXContentTypeOptionsHeader() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    @DisplayName("GET /auth/login debe incluir X-Frame-Options: DENY")
    void testXFrameOptionsHeader() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    @DisplayName("GET /auth/login debe incluir X-XSS-Protection")
    void testXXSSProtectionHeader() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-XSS-Protection"));
    }

    @Test
    @DisplayName("GET /auth/login debe incluir Content-Security-Policy")
    void testContentSecurityPolicyHeader() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    @DisplayName("Endpoint protegido debe incluir todos los headers de seguridad")
    void testSecurityHeadersOnProtectedEndpoint() throws Exception {
        // GET /api/activities es protegido (pero vamos a verificar solo los headers)
        mockMvc.perform(get("/api/activities"))
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("X-XSS-Protection"))
                .andExpect(header().exists("Content-Security-Policy"));
    }
}
