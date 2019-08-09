package com.thoughtworks.gocd.secretmanager.vault.validation;

import cd.go.plugin.base.validation.ValidationResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class AuthMethodValidatorTest {
    @Nested
    class validate {
        @ParameterizedTest
        @ValueSource(strings = {"cert", "approle", "token"})
        void resultShouldNotHaveErrorsForSecretConfigHavingSupportedAuthMethod(String authMethod) {
            Map<String, String> request = singletonMap("AuthMethod", authMethod);

            ValidationResult result = new AuthMethodValidator().validate(request);

            assertThat(result).isEmpty();
        }

        @Test
        void resultShouldHaveErrorsForSecretConfigWithInValidAuthMethod() {
            Map<String, String> request = singletonMap("AuthMethod", "aws");

            ValidationResult result = new AuthMethodValidator().validate(request);

            assertThat(result).hasSize(1);
        }

        @Test
        void shouldIgnoreValidationIfAuthMethodIsNotSpecified() {
            Map<String, String> request = emptyMap();

            ValidationResult result = new AuthMethodValidator().validate(request);

            assertThat(result).isEmpty();
        }
    }
}