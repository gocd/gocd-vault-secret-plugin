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
import com.thoughtworks.gocd.secretmanager.vault.TestUtils;
import com.thoughtworks.gocd.secretmanager.vault.annotations.JsonSource;
import com.thoughtworks.gocd.secretmanager.vault.http.OkHTTPClientFactory;
import com.thoughtworks.gocd.secretmanager.vault.http.exceptions.APIException;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import com.thoughtworks.gocd.secretmanager.vault.request.vault.MetadataRequest;
import com.thoughtworks.gocd.secretmanager.vault.secretengines.OIDCPipelineIdentityProvider;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class VaultSysApiTest {

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
            "/mocks/vault/auth-mounts.json"
    })
    public void createPipelineEntityAliasTest(String secretConfigJson, String authMountsResponse) throws VaultException, IOException, InterruptedException, APIException {
        SecretConfig secretConfig = GsonTransformer.fromJson(secretConfigJson, SecretConfig.class);

        mockWebServer.enqueue(new MockResponse().setBody(authMountsResponse));
        mockWebServer.start();

        VaultConfig vaultConfig = new VaultConfig()
                .address(mockWebServer.url("").url().toString())
                .token("some-token")
                .build();

        VaultSysApi vaultSysApi = new VaultSysApi(
                vaultConfig,
                new OkHTTPClientFactory().vault(secretConfig)
        );

        String authMountAccessor = vaultSysApi.getAuthMountAccessor();

        RecordedRequest authList = mockWebServer.takeRequest();
        assertThat(authList.getPath()).isEqualTo("/v1/sys/auth");
        assertThat(authList.getMethod()).isEqualTo("GET");

        assertThat(authMountAccessor).isEqualTo("auth_token_12ac23");

    }
}
