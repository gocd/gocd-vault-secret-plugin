/*
 * Copyright 2022 ThoughtWorks, Inc.
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

package com.thoughtworks.gocd.secretmanager.vault.api;

import cd.go.plugin.base.GsonTransformer;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.thoughtworks.gocd.secretmanager.vault.annotations.JsonSource;
import com.thoughtworks.gocd.secretmanager.vault.http.OkHTTPClientFactory;
import com.thoughtworks.gocd.secretmanager.vault.http.exceptions.APIException;
import com.thoughtworks.gocd.secretmanager.vault.models.PipelineMaterial;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import com.thoughtworks.gocd.secretmanager.vault.secretengines.OIDCPipelineIdentityProvider;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static com.thoughtworks.gocd.secretmanager.vault.TestUtils.extractBodyAsString;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class VaultAuthApiTest {

    private MockWebServer mockWebServer;

    @BeforeEach
    public void setup() {
        mockWebServer = new MockWebServer();
    }

    @AfterEach
    public void cleanup() throws IOException {
        mockWebServer.shutdown();
    }

    @ParameterizedTest
    @JsonSource(jsonFiles = {
            "/secret-config-oidc.json",
            "/mocks/vault/auth-token.json"
    })
    public void assumePipelineTest(String secretConfigJson, String authTokenResponse) throws VaultException, IOException, InterruptedException, APIException {
        SecretConfig secretConfig = GsonTransformer.fromJson(secretConfigJson, SecretConfig.class);

        mockWebServer.enqueue(new MockResponse().setBody(authTokenResponse));
        mockWebServer.start();

        VaultConfig vaultConfig = new VaultConfig()
                .address(mockWebServer.url("").url().toString())
                .token("some-token")
                .build();
        VaultAuthApi vaultAuthApi = new VaultAuthApi(
                secretConfig,
                vaultConfig,
                new OkHTTPClientFactory().vault(secretConfig)
        );

        String pipelineToken = vaultAuthApi.assumePipeline("some-backend-role", Lists.list("some-policy"), "some_pipelinename");
        assertThat(pipelineToken).isEqualTo("s.wOrq9dO9kzOcuvB06CMviJhZ");

        RecordedRequest tokenCreation = mockWebServer.takeRequest();
        assertThat(tokenCreation.getPath()).isEqualTo("/v1/auth/token/create/some-backend-role");
        assertThat(tokenCreation.getMethod()).isEqualTo("POST");

        String body = extractBodyAsString(tokenCreation);
        assertThat(body).isEqualTo("{\"role_name\":\"some-backend-role\",\"policies\":[\"some-policy\"],\"entity_alias\":\"some_pipelinename\"}");
    }

}