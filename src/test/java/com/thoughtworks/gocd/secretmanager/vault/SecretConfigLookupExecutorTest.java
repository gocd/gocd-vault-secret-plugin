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

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Logical;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.secretmanager.vault.http.exceptions.APIException;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import com.thoughtworks.gocd.secretmanager.vault.request.SecretConfigRequest;
import com.thoughtworks.gocd.secretmanager.vault.secretengines.KVSecretEngine;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@MockitoSettings
class SecretConfigLookupExecutorTest {
    @Mock
    private VaultProvider vaultProvider;
    @Mock
    private Vault vault;
    @Mock
    private Logical logical;

    private SecretConfigLookupExecutor secretConfigLookupExecutor;

    @BeforeEach
    void setUp() throws VaultException {
        when(vaultProvider.vaultFor(any())).thenReturn(vault);

        secretConfigLookupExecutor = spy(new SecretConfigLookupExecutor(vaultProvider));
    }

    @Test
    void shouldReturnLookupResponse() throws APIException, JSONException {
        final SecretConfigRequest request = mock(SecretConfigRequest.class);
        final SecretConfig secretConfig = mock(SecretConfig.class);
        final KVSecretEngine kvSecretEngine = mock(KVSecretEngine.class);
        final VaultConfig vaultConfig = mock(VaultConfig.class);

        when(vaultProvider.getVaultConfig()).thenReturn(vaultConfig);
        when(request.getConfiguration()).thenReturn(secretConfig);
        doReturn(kvSecretEngine).when(secretConfigLookupExecutor).buildSecretEngine(request, vault, vaultConfig);

        when(kvSecretEngine.getSecret(anyString(), eq("AWS_ACCESS_KEY"))).thenReturn(Optional.of("ASKDMDASDKLASDI"));
        when(kvSecretEngine.getSecret(anyString(), eq("AWS_SECRET_KEY"))).thenReturn(Optional.of("slfjskldfjsdjflfsdfsffdadsdfsdfsdfsd;"));

        when(secretConfig.getVaultPath()).thenReturn("/secret/gocd");
        when(request.getKeys()).thenReturn(Arrays.asList("AWS_ACCESS_KEY", "AWS_SECRET_KEY"));

        final GoPluginApiResponse response = secretConfigLookupExecutor.execute(request);

        assertThat(response.responseCode()).isEqualTo(200);
        final String expectedResponse = "[\n" +
                "  {\n" +
                "    \"key\": \"AWS_ACCESS_KEY\",\n" +
                "    \"value\": \"ASKDMDASDKLASDI\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"AWS_SECRET_KEY\",\n" +
                "    \"value\": \"slfjskldfjsdjflfsdfsffdadsdfsdfsdfsd;\"\n" +
                "  }\n" +
                "]";
        assertEquals(expectedResponse, response.responseBody(), true);
    }
}