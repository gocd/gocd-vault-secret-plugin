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
import com.thoughtworks.gocd.secretmanager.vault.request.vault.EntityDataResponse;
import com.thoughtworks.gocd.secretmanager.vault.request.vault.EntityResponse;
import com.thoughtworks.gocd.secretmanager.vault.request.vault.MetadataRequest;
import com.thoughtworks.gocd.secretmanager.vault.secretengines.OIDCPipelineIdentityProvider;
import kotlin.Metadata;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.util.Lists;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.IOException;
import java.util.Optional;

import static com.thoughtworks.gocd.secretmanager.vault.TestUtils.extractBodyAsString;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

public class VaultIdentityApiTest {

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
            "/secret-config-oidc.json"
    })
    public void createPipelineEntityAliasTest(String secretConfigJson) throws VaultException, IOException, InterruptedException, APIException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(204));

        VaultIdentityApi vaultIdentityApi = initVaultIdentityApi(secretConfigJson);

        vaultIdentityApi.createPipelineEntityAlias("7d2e3179-f69b-450c-7179-ac8ee8bd8ca9", "auth_token_12ac23", "some-entity-alias-name");

        RecordedRequest createEntityAlias = mockWebServer.takeRequest();
        assertThat(createEntityAlias.getPath()).isEqualTo("/v1/identity/entity-alias");
        assertThat(createEntityAlias.getMethod()).isEqualTo("POST");

        String body = extractBodyAsString(createEntityAlias);

        assertThat(body).isEqualToNormalizingWhitespace("{\"name\":\"some-entity-alias-name\",\"canonical_id\":\"7d2e3179-f69b-450c-7179-ac8ee8bd8ca9\",\"mount_accessor\":\"auth_token_12ac23\"}");
    }



    @ParameterizedTest
    @JsonSource(jsonFiles = {
            "/secret-config-oidc.json",
            "/mocks/vault/oidc-token.json"
    })
    public void oidcTokenTest(String secretConfigJson, String oidcTokenResponse) throws VaultException, IOException, InterruptedException, APIException {
        mockWebServer.enqueue(new MockResponse().setBody(oidcTokenResponse));

        VaultIdentityApi vaultIdentityApi = initVaultIdentityApi(secretConfigJson);

        String oidcToken = vaultIdentityApi.oidcToken("some_token", "/v1/identity/oidc/token/some-oidc-backend");
        assertThat(oidcToken).isEqualTo("eyJhbGciOiJSUzI1NiIsImtpZCI6IjJkMGI4YjlkLWYwNGQtNzFlYy1iNjc0LWM3MzU4NDMyYmM1YiJ9.eyJhdWQiOiJQNkNmQ3p5SHNRWTRwTWNBNmtXQU9DSXRBNyIsImV4cCI6MTU2MTQ4ODQxMiwiaWF0IjoxNTYxNDAyMDEyLCJpc3MiOiJodHRwczovL2V4YW1wbGUuY29tOjEyMzQiLCJzdWIiOiI2YzY1ZWFmNy1kNGY0LTEzMzMtMDJiYy0xYzc1MjE5YzMxMDIifQ.IcbWTmks7P5eVtwmIBl5rL1B88MI55a9JJuYVLIlwE9aP_ilXpX5fE38CDm5PixDDVJb8TI2Q_FO4GMMH0ymHDO25ZvA917WcyHCSBGaQlgcS-WUL2fYTqFjSh-pezszaYBgPuGvH7hJjlTZO6g0LPCyUWat3zcRIjIQdXZum-OyhWAelQlveEL8sOG_ldyZ8v7fy7GXDxfJOK1kpw5AX9DXJKylbwZTBS8tLb-7edq8uZ0lNQyWy9VPEW_EEIZvGWy0AHua-Loa2l59GRRP8mPxuMYxH_c88x1lsSw0vH9E3rU8AXLyF3n4d40PASXEjZ-7dnIf4w4hf2P4L0xs_g");

        RecordedRequest tokenCreation = mockWebServer.takeRequest();
        assertThat(tokenCreation.getPath()).isEqualTo("/v1/identity/oidc/token/some-oidc-backend");
        assertThat(tokenCreation.getMethod()).isEqualTo("GET");
        assertThat(tokenCreation.getHeader("X-Vault-Token")).isEqualTo("some_token");
    }

    @ParameterizedTest
    @JsonSource(jsonFiles = {
            "/secret-config-oidc.json",
            "/mocks/vault/entity.json"
    })
    public void createPipelineEntityTestOnNewEntity(String secretConfig, String entityResponse) throws VaultException, IOException, APIException, InterruptedException {
        PipelineMaterial metadata = new PipelineMaterial(
                "some_pipelinename",
                "some_group",
                "some_organization",
                "some_repository",
                "some_branch"
        );


        mockWebServer.enqueue(new MockResponse().setBody(entityResponse));

        VaultIdentityApi vaultIdentityApi = initVaultIdentityApi(secretConfig);

        Optional<EntityResponse> pipelineEntity = vaultIdentityApi.createPipelineEntity("pipeline-identity-dev-test-pipeline", Lists.list("gocd-pipeline-policy-dev-test"), metadata);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/v1/identity/entity/name/pipeline-identity-dev-test-pipeline");
        String recordedRequestString = extractBodyAsString(recordedRequest);
        assertThat(recordedRequestString).isEqualTo("{\"name\":\"pipeline-identity-dev-test-pipeline\",\"policies\":[\"gocd-pipeline-policy-dev-test\"],\"metadata\":{\"group\":\"some_group\",\"pipeline\":\"some_pipelinename\",\"repository\":\"some_repository\",\"organization\":\"some_organization\",\"branch\":\"some_branch\"}}");

        assertThat(pipelineEntity).isPresent();
        EntityDataResponse data = pipelineEntity.get().getData();
        assertThat(data.getName()).isEqualTo("pipeline-identity-dev-test-pipeline");
        assertThat(data.getId()).isEqualTo("1ab2dbd4-ff87-8291-5e08-56a0083424e1");
    }

    @ParameterizedTest
    @JsonSource(jsonFiles = {
            "/secret-config-oidc.json"
    })
    public void createPipelineEntityTestOnExistingEntity(String secretConfig) throws VaultException, IOException, APIException, InterruptedException {
        PipelineMaterial metadata = new PipelineMaterial(
                "some_pipelinename",
                "some_group",
                "some_organization",
                "some_repository",
                "some_branch"
        );


        mockWebServer.enqueue(new MockResponse().setResponseCode(204));

        VaultIdentityApi vaultIdentityApi = initVaultIdentityApi(secretConfig);

        Optional<EntityResponse> pipelineEntity = vaultIdentityApi.createPipelineEntity("pipeline-identity-dev-test-pipeline", Lists.list("gocd-pipeline-policy-dev-test"), metadata);

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getPath()).isEqualTo("/v1/identity/entity/name/pipeline-identity-dev-test-pipeline");
        String recordedRequestString = extractBodyAsString(recordedRequest);
        assertThat(recordedRequestString).isEqualTo("{\"name\":\"pipeline-identity-dev-test-pipeline\",\"policies\":[\"gocd-pipeline-policy-dev-test\"],\"metadata\":{\"group\":\"some_group\",\"pipeline\":\"some_pipelinename\",\"repository\":\"some_repository\",\"organization\":\"some_organization\",\"branch\":\"some_branch\"}}");

        assertThat(pipelineEntity).isNotPresent();
    }

    @ParameterizedTest
    @JsonSource(jsonFiles = {
            "/secret-config-oidc.json",
            "/mocks/vault/entity.json"
    })
    public void getPipelineEntityTest(String secretConfig, String entityResponse) throws VaultException, IOException, APIException, InterruptedException {
        mockWebServer.enqueue(new MockResponse().setBody(entityResponse));

        VaultIdentityApi vaultIdentityApi = initVaultIdentityApi(secretConfig);

        EntityResponse pipelineEntity = vaultIdentityApi.fetchPipelineEntity("pipeline-identity-dev-test-pipeline");

        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getPath()).isEqualTo("/v1/identity/entity/name/pipeline-identity-dev-test-pipeline");

        assertThat(pipelineEntity.getData().getName()).isEqualTo("pipeline-identity-dev-test-pipeline");
        assertThat(pipelineEntity.getData().getId()).isEqualTo("1ab2dbd4-ff87-8291-5e08-56a0083424e1");
    }

    private VaultIdentityApi initVaultIdentityApi(String secretConfigJson) throws IOException, VaultException {
        SecretConfig secretConfig = GsonTransformer.fromJson(secretConfigJson, SecretConfig.class);

        mockWebServer.start();
        VaultConfig vaultConfig = new VaultConfig()
                .address(mockWebServer.url("").url().toString())
                .token("some-token")
                .build();

        VaultIdentityApi vaultIdentityApi = new VaultIdentityApi(
                vaultConfig,
                new OkHTTPClientFactory().vault(secretConfig)
        );
        return vaultIdentityApi;
    }
}
