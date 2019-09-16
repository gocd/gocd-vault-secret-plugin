/*
 * Copyright 2019 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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