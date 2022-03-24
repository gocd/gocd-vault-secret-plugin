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

package com.thoughtworks.gocd.secretmanager.vault.secretengines;

import cd.go.plugin.base.GsonTransformer;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;
import com.thoughtworks.gocd.secretmanager.vault.annotations.JsonSource;
import com.thoughtworks.gocd.secretmanager.vault.http.exceptions.APIException;
import com.thoughtworks.gocd.secretmanager.vault.models.PipelineMaterial;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import com.thoughtworks.gocd.secretmanager.vault.request.vault.CustomMetadataRequest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

class OIDCPipelineIdentityProviderTest {

    private VaultConfig vaultConfig;
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
            "/mocks/vault/lookup-self.json",
            "/mocks/vault/auth-mounts.json"
    })
    public void createPipelineEntityAliasTest(String secretConfigJson, String lookupSelfResponse, String authMountsResponse) throws VaultException, IOException, InterruptedException, APIException {
        SecretConfig secretConfig = GsonTransformer.fromJson(secretConfigJson, SecretConfig.class);

        mockWebServer.enqueue(new MockResponse().setBody(lookupSelfResponse));
        mockWebServer.enqueue(new MockResponse().setBody(authMountsResponse));
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));

        mockWebServer.start();

        vaultConfig = new VaultConfig()
                .address(mockWebServer.url("").url().toString())
                .token("some-token")
                .build();

        OIDCPipelineIdentityProvider oidcPipelineIdentityProvider = new OIDCPipelineIdentityProvider(
                mock(Vault.class),
                vaultConfig,
                secretConfig
        );

        CustomMetadataRequest customMetadataRequest = new CustomMetadataRequest(
                "some_group",
                "some_pipelinename",
                "some_repository",
                "some_organization",
                "some_branch"
        );

        oidcPipelineIdentityProvider.createPipelineEntityAlias(customMetadataRequest);

        RecordedRequest lookupself = mockWebServer.takeRequest();
        assertThat(lookupself.getPath()).isEqualTo("/v1/auth/token/lookup-self");
        assertThat(lookupself.getMethod()).isEqualTo("GET");

        RecordedRequest authList = mockWebServer.takeRequest();
        assertThat(authList.getPath()).isEqualTo("/v1/sys/auth");
        assertThat(authList.getMethod()).isEqualTo("GET");

        RecordedRequest createEntityAlias = mockWebServer.takeRequest();
        assertThat(createEntityAlias.getPath()).isEqualTo("/v1/identity/entity-alias");
        assertThat(createEntityAlias.getMethod()).isEqualTo("POST");

        String body = extractBodyAsString(createEntityAlias);

        assertThat(body).isEqualToNormalizingWhitespace("{\"name\":\"gocd-pipeline-dev-test-some_pipelinename\",\"canonical_id\":\"7d2e3179-f69b-450c-7179-ac8ee8bd8ca9\",\"mount_accessor\":\"auth_token_12ac23\",\"custom_metadata\":{\"group\":\"some_group\",\"pipeline\":\"some_pipelinename\",\"repository\":\"some_repository\",\"organization\":\"some_organization\",\"branch\":\"some_branch\"}}\n");
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
        vaultConfig = new VaultConfig()
                .address(mockWebServer.url("").url().toString())
                .token("some-token")
                .build();
        OIDCPipelineIdentityProvider oidcPipelineIdentityProvider = new OIDCPipelineIdentityProvider(
                mock(Vault.class),
                vaultConfig,
                secretConfig
        );

        String pipelineToken = oidcPipelineIdentityProvider.assumePipeline("some_pipelinename");
        assertThat(pipelineToken).isEqualTo("s.wOrq9dO9kzOcuvB06CMviJhZ");

        RecordedRequest tokenCreation = mockWebServer.takeRequest();
        assertThat(tokenCreation.getPath()).isEqualTo("/v1/auth/token/create/some-backend-role");
        assertThat(tokenCreation.getMethod()).isEqualTo("POST");

        String body = extractBodyAsString(tokenCreation);
        assertThat(body).isEqualTo("{\"role_name\":\"some-backend-role\",\"policies\":[\"some-policy\"],\"entity_alias\":\"gocd-pipeline-dev-test-some_pipelinename\"}");
    }

    @ParameterizedTest
    @JsonSource(jsonFiles = {
            "/secret-config-oidc.json",
            "/mocks/vault/oidc-token.json"
    })
    public void oidcTokenTest(String secretConfigJson, String oidcTokenResponse) throws VaultException, IOException, InterruptedException, APIException {
        SecretConfig secretConfig = GsonTransformer.fromJson(secretConfigJson, SecretConfig.class);

        mockWebServer.enqueue(new MockResponse().setBody(oidcTokenResponse));
        mockWebServer.start();
        vaultConfig = new VaultConfig()
                .address(mockWebServer.url("").url().toString())
                .token("some-token")
                .build();
        OIDCPipelineIdentityProvider oidcPipelineIdentityProvider = new OIDCPipelineIdentityProvider(
                mock(Vault.class),
                vaultConfig,
                secretConfig
        );

        String oidcToken = oidcPipelineIdentityProvider.oidcToken("some_token", "/v1/identity/oidc/token/some-oidc-backend");
        assertThat(oidcToken).isEqualTo("eyJhbGciOiJSUzI1NiIsImtpZCI6IjJkMGI4YjlkLWYwNGQtNzFlYy1iNjc0LWM3MzU4NDMyYmM1YiJ9.eyJhdWQiOiJQNkNmQ3p5SHNRWTRwTWNBNmtXQU9DSXRBNyIsImV4cCI6MTU2MTQ4ODQxMiwiaWF0IjoxNTYxNDAyMDEyLCJpc3MiOiJodHRwczovL2V4YW1wbGUuY29tOjEyMzQiLCJzdWIiOiI2YzY1ZWFmNy1kNGY0LTEzMzMtMDJiYy0xYzc1MjE5YzMxMDIifQ.IcbWTmks7P5eVtwmIBl5rL1B88MI55a9JJuYVLIlwE9aP_ilXpX5fE38CDm5PixDDVJb8TI2Q_FO4GMMH0ymHDO25ZvA917WcyHCSBGaQlgcS-WUL2fYTqFjSh-pezszaYBgPuGvH7hJjlTZO6g0LPCyUWat3zcRIjIQdXZum-OyhWAelQlveEL8sOG_ldyZ8v7fy7GXDxfJOK1kpw5AX9DXJKylbwZTBS8tLb-7edq8uZ0lNQyWy9VPEW_EEIZvGWy0AHua-Loa2l59GRRP8mPxuMYxH_c88x1lsSw0vH9E3rU8AXLyF3n4d40PASXEjZ-7dnIf4w4hf2P4L0xs_g");

        RecordedRequest tokenCreation = mockWebServer.takeRequest();
        assertThat(tokenCreation.getPath()).isEqualTo("/v1/identity/oidc/token/some-oidc-backend");
        assertThat(tokenCreation.getMethod()).isEqualTo("GET");
        assertThat(tokenCreation.getHeader("X-Vault-Token")).isEqualTo("some_token");
    }

    @NotNull
    private String extractBodyAsString(RecordedRequest request) {
        return new BufferedReader(new InputStreamReader(request.getBody().inputStream(), StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining(""));
    }


}