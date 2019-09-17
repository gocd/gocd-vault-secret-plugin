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

package com.thoughtworks.gocd.secretmanager.vault;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.DefaultGoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.secretmanager.vault.annotations.JsonSource;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.util.Map;

import static cd.go.plugin.base.ResourceReader.readResource;
import static cd.go.plugin.base.ResourceReader.readResourceBytes;
import static java.util.Base64.getDecoder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class VaultPluginTest {
    private VaultPlugin vaultPlugin;

    @BeforeEach
    void setUp() {
        vaultPlugin = new VaultPlugin();
        vaultPlugin.initializeGoApplicationAccessor(mock(GoApplicationAccessor.class));
    }

    @Test
    void shouldReturnPluginIdentifier() {
        assertThat(vaultPlugin.pluginIdentifier()).isNotNull();
        assertThat(vaultPlugin.pluginIdentifier().getExtension()).isEqualTo("secrets");
        assertThat(vaultPlugin.pluginIdentifier().getSupportedExtensionVersions())
                .contains("1.0");
    }

    @ParameterizedTest
    @JsonSource(jsonFiles = "/secret-config-metadata.json")
    void shouldReturnConfigMetadata(String expectedJson) throws UnhandledRequestTypeException, JSONException {
        final DefaultGoPluginApiRequest request = request("go.cd.secrets.secrets-config.get-metadata");

        final GoPluginApiResponse response = vaultPlugin.handle(request);

        assertThat(response.responseCode()).isEqualTo(200);
        assertEquals(expectedJson, response.responseBody(), true);
    }

    @Test
    void shouldReturnIcon() throws UnhandledRequestTypeException {
        final DefaultGoPluginApiRequest request = request("go.cd.secrets.get-icon");

        final GoPluginApiResponse response = vaultPlugin.handle(request);

        Map<String, String> responseBody = toMap(response.responseBody());

        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(responseBody.size()).isEqualTo(2);
        assertThat(responseBody.get("content_type")).isEqualTo("image/png");
        assertThat(getDecoder().decode(responseBody.get("data"))).isEqualTo(readResourceBytes("/plugin-icon.png"));
    }

    @Test
    void shouldReturnSecretConfigView() throws UnhandledRequestTypeException {
        final DefaultGoPluginApiRequest request = request("go.cd.secrets.secrets-config.get-view");

        final GoPluginApiResponse response = vaultPlugin.handle(request);

        Map<String, String> responseBody = toMap(response.responseBody());

        assertThat(response.responseCode()).isEqualTo(200);
        assertThat(responseBody.size()).isEqualTo(1);
        assertThat(responseBody.get("template")).isEqualTo(readResource("/secrets.template.html"));
    }

    @Nested
    class ValidateSecretConfig {
        private String requestName;

        @BeforeEach
        void setUp() {
            requestName = "go.cd.secrets.secrets-config.validate";
        }

        @ParameterizedTest
        @JsonSource(jsonFiles = {
                "/secret-config-with-unknown-fields.json",
                "/unknown-fields-error.json"
        })
        void shouldFailIfHasUnknownFields(String requestBody, String expected) throws UnhandledRequestTypeException, JSONException {
            final DefaultGoPluginApiRequest request = request(requestName);
            request.setRequestBody(requestBody);

            final GoPluginApiResponse response = vaultPlugin.handle(request);

            assertThat(response.responseCode()).isEqualTo(200);
            assertEquals(expected, response.responseBody(), true);
        }

        @ParameterizedTest
        @JsonSource(jsonFiles = {
                "/secret-config-with-missing-required-fields.json",
                "/missing-fields-error.json"
        })
        void shouldFailIfRequiredFieldsAreMissingInRequestBody(String requestBody, String expected) throws UnhandledRequestTypeException, JSONException {
            final DefaultGoPluginApiRequest request = request(requestName);
            request.setRequestBody(requestBody);

            final GoPluginApiResponse response = vaultPlugin.handle(request);

            assertThat(response.responseCode()).isEqualTo(200);
            assertEquals(expected, response.responseBody(), true);
        }

        @ParameterizedTest
        @JsonSource(jsonFiles = "/secret-config.json")
        void shouldPassIfRequestIsValid(String requestBody) throws JSONException, UnhandledRequestTypeException {
            final DefaultGoPluginApiRequest request = request(requestName);
            request.setRequestBody(requestBody);

            final GoPluginApiResponse response = vaultPlugin.handle(request);

            assertThat(response.responseCode()).isEqualTo(200);
            assertEquals("[]", response.responseBody(), true);
        }
    }

    private Map<String, String> toMap(String response) {
        return new Gson().fromJson(response, new TypeToken<Map<String, String>>() {
        }.getType());
    }

    private DefaultGoPluginApiRequest request(String requestName) {
        return new DefaultGoPluginApiRequest("secrets", "1.0", requestName);
    }
}