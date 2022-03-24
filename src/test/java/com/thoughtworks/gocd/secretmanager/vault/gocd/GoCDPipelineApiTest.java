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

package com.thoughtworks.gocd.secretmanager.vault.gocd;

import cd.go.plugin.base.GsonTransformer;
import com.thoughtworks.gocd.secretmanager.vault.annotations.JsonSource;
import com.thoughtworks.gocd.secretmanager.vault.http.exceptions.APIException;
import com.thoughtworks.gocd.secretmanager.vault.models.PipelineMaterial;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GoCDPipelineApiTest {

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
            "/mocks/gocd/pipeline-config.json"
    })
    public void fetchPipelineMaterialTestSucceedsForGitMaterial(String secretConfigJson, String pipelineConfigResponse) throws IOException, InterruptedException, APIException {
        SecretConfig secretConfig = spy(GsonTransformer.fromJson(secretConfigJson, SecretConfig.class));

        mockWebServer.enqueue(new MockResponse().setBody(pipelineConfigResponse));
        mockWebServer.start();

        doReturn(getMockServerAddress()).when(secretConfig).getGocdServerURL();
        GoCDPipelineApi goCDPipelineApi = new GoCDPipelineApi(secretConfig);

        PipelineMaterial pipelineMaterial = goCDPipelineApi.fetchPipelineMaterial("some-pipeline");
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertThat(recordedRequest.getHeader("Authorization")).isEqualTo("Basic " + Base64.getEncoder().encodeToString("username:supersecret".getBytes(StandardCharsets.UTF_8)));
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(recordedRequest.getHeader("Accept")).isEqualTo(GoCDPipelineApi.ACCEPT_GOCD_V11_JSON.toString());
        assertThat(recordedRequest.getPath()).isEqualTo("/go/api/admin/pipelines/some-pipeline");

        assertThat(pipelineMaterial).isEqualTo(
                new PipelineMaterial(
                        "some-pipeline",
                        "dev",
                        "important-organization",
                        "some-repository",
                        "main"
                )
        );
    }

    @ParameterizedTest
    @JsonSource(jsonFiles = {
            "/secret-config-oidc.json",
            "/mocks/gocd/pipeline-config-scm.json",
            "/mocks/gocd/scm-response.json"
    })
    public void fetchPipelineMaterialTestSucceedsForSCMMaterial(String secretConfigJson, String pipelineConfigResponse, String scmResponse) throws IOException, InterruptedException, APIException {
        SecretConfig secretConfig = spy(GsonTransformer.fromJson(secretConfigJson, SecretConfig.class));

        mockWebServer.enqueue(new MockResponse().setBody(pipelineConfigResponse));
        mockWebServer.enqueue(new MockResponse().setBody(scmResponse));
        mockWebServer.start();

        doReturn(getMockServerAddress()).when(secretConfig).getGocdServerURL();
        GoCDPipelineApi goCDPipelineApi = new GoCDPipelineApi(secretConfig);

        PipelineMaterial pipelineMaterial = goCDPipelineApi.fetchPipelineMaterial("some-pipeline");
        RecordedRequest pipelineConfigrequest = mockWebServer.takeRequest();

        String basicAuth = "Basic " + Base64.getEncoder().encodeToString("username:supersecret".getBytes(StandardCharsets.UTF_8));
        assertThat(pipelineConfigrequest.getHeader("Authorization")).isEqualTo(basicAuth);
        assertThat(pipelineConfigrequest.getMethod()).isEqualTo("GET");
        assertThat(pipelineConfigrequest.getHeader("Accept")).isEqualTo(GoCDPipelineApi.ACCEPT_GOCD_V11_JSON.toString());
        assertThat(pipelineConfigrequest.getPath()).isEqualTo("/go/api/admin/pipelines/some-pipeline");

        RecordedRequest scmConfigRequest = mockWebServer.takeRequest();
        assertThat(scmConfigRequest.getHeader("Authorization")).isEqualTo(basicAuth);
        assertThat(scmConfigRequest.getMethod()).isEqualTo("GET");
        assertThat(scmConfigRequest.getHeader("Accept")).isEqualTo(GoCDPipelineApi.ACCEPT_GOCD_V4_JSON.toString());
        assertThat(scmConfigRequest.getPath()).isEqualTo("/go/api/admin/scms/some-pipeline");


        assertThat(pipelineMaterial).isEqualTo(
                new PipelineMaterial(
                        "some-pipeline",
                        "dev",
                        "important-organization",
                        "some-repository",
                        null
                )
        );
    }

    private String getMockServerAddress() {
        String address = mockWebServer.url("").url().toString();
        return address.substring(0, address.length() - 1);
    }

}