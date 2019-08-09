package com.thoughtworks.gocd.secretmanager.vault.validation;

import cd.go.plugin.base.validation.ValidationResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class TokenAuthMethodValidatorTest {
    @Nested
    class validate {
        @Test
        void resultShouldHaveErrorsIfTokenNotSpecifiedInSecretConfig() {
            Map<String, String> request = singletonMap("AuthMethod", "token");

            ValidationResult result = new TokenAuthMethodValidator().validate(request);

            assertThat(result).isNotEmpty();
        }

        @Test
        void shouldSkipValidationForNonTokenAuthMethod() {
            Map<String, String> request = singletonMap("AuthMethod", "cert");

            ValidationResult result = new TokenAuthMethodValidator().validate(request);

            assertThat(result).isEmpty();
        }

        @Test
        void resultShouldNotHaveErrorsIfTokenSpecified() {
            Map<String, String> request = new HashMap<>();
            request.put("AuthMethod", "token");
            request.put("Token", "some_token");

            ValidationResult result = new TokenAuthMethodValidator().validate(request);

            assertThat(result).isEmpty();
        }
    }
}