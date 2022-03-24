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

package com.thoughtworks.gocd.secretmanager.vault.http;

import cd.go.plugin.base.GsonTransformer;
import com.thoughtworks.gocd.secretmanager.vault.annotations.JsonSource;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;

import java.io.IOException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

public class OkHTTPClientTest {

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
    @JsonSource(jsonFiles = "/secret-config-network-settings.json")
    public void requestSucceedWithDefaultContentTypeAdded(String secretConfigJson) throws InterruptedException, IOException {
        SecretConfig secretConfig = GsonTransformer.fromJson(secretConfigJson, SecretConfig.class);
        OkHTTPClientFactory okHTTPClientFactory = new OkHTTPClientFactory();
        OkHttpClient okHttpClient = okHTTPClientFactory.vault(secretConfig);

        mockWebServer.enqueue(new MockResponse());

        mockWebServer.start();

        okHttpClient.newCall(new Request.Builder().url(mockWebServer.url("")).build()).execute();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertThat(recordedRequest.getHeader("Content-Type")).isEqualTo(OkHTTPClientFactory.CONTENT_TYPE_JSON.toString());
    }

    @ParameterizedTest
    @JsonSource(jsonFiles = "/secret-config-network-settings.json")
    public void requestSucceedWithRetries(String secretConfigJson) throws IOException {
        SecretConfig secretConfig = GsonTransformer.fromJson(secretConfigJson, SecretConfig.class);
        OkHTTPClientFactory okHTTPClientFactory = new OkHTTPClientFactory();
        OkHttpClient okHttpClient = okHTTPClientFactory.vault(secretConfig);

        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse());

        mockWebServer.start();

        Response response = okHttpClient.newCall(new Request.Builder().url(mockWebServer.url("")).build()).execute();

        assertThat(mockWebServer.getRequestCount()).isEqualTo(4);
        assertThat(response.code()).isEqualTo(200);
    }

    @ParameterizedTest
    @JsonSource(jsonFiles = "/secret-config-network-settings.json")
    public void requestFailsWithMaxRetriesExceeded(String secretConfigJson) throws IOException {
        SecretConfig secretConfig = GsonTransformer.fromJson(secretConfigJson, SecretConfig.class);
        OkHTTPClientFactory okHTTPClientFactory = new OkHTTPClientFactory();
        OkHttpClient okHttpClient = okHTTPClientFactory.vault(secretConfig);

        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse());

        mockWebServer.start();

        Response response = okHttpClient.newCall(new Request.Builder().url(mockWebServer.url("")).build()).execute();

        assertThat(mockWebServer.getRequestCount()).isEqualTo(4);
        assertThat(response.code()).isEqualTo(500);
    }

    @ParameterizedTest
    @JsonSource(jsonFiles = "/secret-config-network-settings.json")
    public void requestSucceedsWithAddingNamespaceHeader(String secretConfigJson) throws IOException, InterruptedException {
        SecretConfig secretConfig = GsonTransformer.fromJson(secretConfigJson, SecretConfig.class);
        OkHTTPClientFactory okHTTPClientFactory = new OkHTTPClientFactory();
        OkHttpClient okHttpClient = okHTTPClientFactory.vault(secretConfig);

        mockWebServer.enqueue(new MockResponse());

        mockWebServer.start();

        okHttpClient.newCall(new Request.Builder().url(mockWebServer.url("")).build()).execute();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertThat(recordedRequest.getHeader("X-Vault-Namespace")).isEqualTo("dev");
    }

    @ParameterizedTest
    @JsonSource(jsonFiles = "/secret-config-network-settings.json")
    public void requestSucceedsWithNotAddingNamespaceHeader(String secretConfigJson) throws IOException, InterruptedException {
        SecretConfig secretConfig = spy(GsonTransformer.fromJson(secretConfigJson, SecretConfig.class));
        OkHTTPClientFactory okHTTPClientFactory = new OkHTTPClientFactory();
        doReturn("").when(secretConfig).getNameSpace();
        OkHttpClient okHttpClient = okHTTPClientFactory.vault(secretConfig);

        mockWebServer.enqueue(new MockResponse());

        mockWebServer.start();

        okHttpClient.newCall(new Request.Builder().url(mockWebServer.url("")).build()).execute();

        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertThat(recordedRequest.getHeader("X-Vault-Namespace")).isNull();
    }
}
