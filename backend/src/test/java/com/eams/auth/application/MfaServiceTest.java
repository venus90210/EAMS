package com.eams.auth.application;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@Tag("unit")
class MfaServiceTest {

    private MfaService mfaService;

    @BeforeEach
    void setUp() {
        mfaService = new MfaService();
    }

    @Test
    void generateSecret_returnsNonBlankString() {
        String secret = mfaService.generateSecret();
        assertThat(secret).isNotBlank();
    }

    @Test
    void generateSecret_returnsUniqueValues() {
        String s1 = mfaService.generateSecret();
        String s2 = mfaService.generateSecret();
        assertThat(s1).isNotEqualTo(s2);
    }

    @Test
    void getOtpAuthUrl_containsIssuerAndEmail() {
        String secret = mfaService.generateSecret();
        String url = mfaService.getOtpAuthUrl(secret, "admin@test.com");

        assertThat(url)
                .contains("EAMS")
                .contains("admin@test.com")
                .startsWith("otpauth://totp/");
    }

    @Test
    void verifyCode_returnsTrue_forValidCode() {
        // Genera un secreto y obtiene el código actual directamente con googleauth
        String secret = mfaService.generateSecret();
        GoogleAuthenticator gauth = new GoogleAuthenticator();
        int validCode = gauth.getTotpPassword(secret);

        assertThat(mfaService.verifyCode(secret, validCode)).isTrue();
    }

    @Test
    void verifyCode_returnsFalse_forInvalidCode() {
        String secret = mfaService.generateSecret();
        assertThat(mfaService.verifyCode(secret, 000000)).isFalse();
    }

    @Test
    void verifyCode_returnsFalse_forWrongSecret() {
        String secret1 = mfaService.generateSecret();
        String secret2 = mfaService.generateSecret();
        GoogleAuthenticator gauth = new GoogleAuthenticator();
        int codeForSecret1 = gauth.getTotpPassword(secret1);

        // El código de secret1 no debe validar contra secret2
        assertThat(mfaService.verifyCode(secret2, codeForSecret1)).isFalse();
    }
}
