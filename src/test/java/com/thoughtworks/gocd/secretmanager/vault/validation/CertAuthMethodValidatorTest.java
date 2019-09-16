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

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class CertAuthMethodValidatorTest {
    @Nested
    class validate {
        @Test
        void resultShouldHaveErrorsIfClientKeyIsNotSpecifiedInSecretConfig() {
            Map<String, String> request = singletonMap("AuthMethod", "cert");

            ValidationResult result = new CertAuthMethodValidator().validate(request);

            assertThat(result).isNotEmpty();
        }

        @Test
        void shouldSkipValidationForNonTokenAuthMethod() {
            Map<String, String> request = singletonMap("AuthMethod", "approle");

            ValidationResult result = new CertAuthMethodValidator().validate(request);

            assertThat(result).isEmpty();
        }

        @Test
        void resultShouldNotHaveErrorsIfClientKeyAndClientCertAreSpecified() {
            Map<String, String> request = new HashMap<>();
            request.put("AuthMethod", "cert");
            request.put("ClientPem", "some_client");
            request.put("ClientKeyPem", "some_client_key");


            ValidationResult result = new CertAuthMethodValidator().validate(request);

            assertThat(result).isEmpty();
        }
    }
}