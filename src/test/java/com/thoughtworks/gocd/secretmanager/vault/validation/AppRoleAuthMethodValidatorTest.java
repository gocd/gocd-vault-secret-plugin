package com.thoughtworks.gocd.secretmanager.vault.validation;

import cd.go.plugin.base.validation.ValidationResult;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class AppRoleAuthMethodValidatorTest {
    @Nested
    class validate {
        @Test
        void resultShouldHaveErrorsIfRoleIdIsNotSpecifiedInSecretConfig() {
            Map<String, String> request = singletonMap("AuthMethod", "approle");

            ValidationResult result = new AppRoleAuthMethodValidator().validate(request);

            assertThat(result).isNotEmpty();
        }

        @Test
        void shouldSkipValidationForNonTokenAuthMethod() {
            Map<String, String> request = singletonMap("AuthMethod", "cert");

            ValidationResult result = new AppRoleAuthMethodValidator().validate(request);

            assertThat(result).isEmpty();
        }

        @Test
        void resultShouldNotHaveErrorsIfTokenSpecified() {
            Map<String, String> request = new HashMap<>();
            request.put("AuthMethod", "approle");
            request.put("RoleId", "some_role_id");
            request.put("SecretId", "some_secret_id");

            ValidationResult result = new AppRoleAuthMethodValidator().validate(request);

            assertThat(result).isEmpty();
        }
    }
}