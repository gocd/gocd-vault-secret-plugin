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
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.gocd.secretmanager.vault.gocd.GoCDPipelineApi;
import com.thoughtworks.gocd.secretmanager.vault.http.DataResponseExtractor;
import com.thoughtworks.gocd.secretmanager.vault.http.OkHTTPClientFactory;
import com.thoughtworks.gocd.secretmanager.vault.http.exceptions.APIException;
import com.thoughtworks.gocd.secretmanager.vault.models.PipelineMaterial;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import com.thoughtworks.gocd.secretmanager.vault.request.vault.*;
import okhttp3.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Optional;

import static cd.go.plugin.base.GsonTransformer.toJson;
import static com.thoughtworks.gocd.secretmanager.vault.http.OkHTTPClientFactory.CONTENT_TYPE_JSON;

public class OIDCPipelineIdentityProvider extends SecretEngine {

    private final OkHttpClient client;
    private final DataResponseExtractor dataResponseExtractor;
    private final GoCDPipelineApi gocdPipelineApi;
    private VaultConfig vaultConfig;
    private SecretConfig secretConfig;

    private static final String X_VAULT_TOKEN = "X-Vault-Token";

    public OIDCPipelineIdentityProvider(Vault vault, VaultConfig vaultConfig, SecretConfig secretConfig) {
        super(vault);
        this.vaultConfig = vaultConfig;
        this.secretConfig = secretConfig;
        this.gocdPipelineApi = new GoCDPipelineApi(secretConfig);
        this.dataResponseExtractor = new DataResponseExtractor();

        OkHTTPClientFactory okHTTPClientFactory = new OkHTTPClientFactory();
        this.client = okHTTPClientFactory.vault(secretConfig);
    }

    @Override
    public Optional<String> getSecret(String path, String pipelineName) throws APIException {
        PipelineMaterial pipelineMaterial = gocdPipelineApi.fetchPipelineMaterial(pipelineName);
        CustomMetadataRequest customMetadataRequest = new CustomMetadataRequest(pipelineMaterial);

        createPipelineEntityAlias(customMetadataRequest);

        String pipelineAuthToken = assumePipeline(customMetadataRequest.getPipeline());
        return Optional.of(oidcToken(pipelineAuthToken, path));
    }

    protected void createPipelineEntityAlias(CustomMetadataRequest customMetadataRequest) throws APIException {
        String entityId = getEntityId();
        String mountAccessor = getMountAccessor();

        EntityAliasRequest entityAliasRequest = new EntityAliasRequest(
                entityAliasName(customMetadataRequest.getPipeline()),
                entityId,
                mountAccessor,
                customMetadataRequest
        );

        RequestBody body = RequestBody.create(toJson(entityAliasRequest), CONTENT_TYPE_JSON);
        Request request = new Request.Builder()
                .header(X_VAULT_TOKEN, vaultConfig.getToken())
                .url(vaultConfig.getAddress() + "/v1/identity/entity-alias")
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();

            if (response.code() < 200 || response.code() >= 300) {
                throw new APIException("Could not create entity alias. Due to: " + response.body().string(), response.code());
            }
        } catch (IOException e) {
            throw new APIException(e);
        }
    }

    private String getMountAccessor() throws APIException {
        Request request = new Request.Builder()
                .header(X_VAULT_TOKEN, vaultConfig.getToken())
                .url(vaultConfig.getAddress() + "/v1/sys/auth")
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();

            if (response.code() < 200 || response.code() >= 300) {
                throw new APIException("Could not fetch auth mounts own token. Due to: " + response.body().string(), response.code());
            }

            AuthMountsResponse authMountsResponse = GsonTransformer.fromJson(response.body().string(), AuthMountsResponse.class);
            return authMountsResponse.getToken().getAccessor();
        } catch (IOException e) {
            throw new APIException(e);
        }
    }

    private String getEntityId() throws APIException {
        Request request = new Request.Builder()
                .header(X_VAULT_TOKEN, vaultConfig.getToken())
                .url(vaultConfig.getAddress() + "/v1/auth/token/lookup-self")
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();

            if (response.code() < 200 || response.code() >= 300) {
                throw new APIException("Could not lookup own token. Due to: " + response.body().string(), response.code());
            }

            LookupResponse lookupResponse = dataResponseExtractor.extract(response, LookupResponse.class);
            return lookupResponse.getEntityId();
        } catch (IOException e) {
            throw new APIException(e);
        }
    }

    protected String assumePipeline(String pipelineName) throws APIException {
        CreateTokenRequest createTokenRequest = new CreateTokenRequest(
                secretConfig.getPipelineTokenAuthBackendRole(),
                secretConfig.getPipelinePolicy().isEmpty() ? null : secretConfig.getPipelinePolicy(),
                entityAliasName(pipelineName)
        );

        RequestBody body = RequestBody.create(toJson(createTokenRequest), CONTENT_TYPE_JSON);
        Request request = new Request.Builder()
                .header(X_VAULT_TOKEN, vaultConfig.getToken())
                .url(vaultConfig.getAddress() + "/v1/auth/token/create/" + secretConfig.getPipelineTokenAuthBackendRole())
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();

            if (response.code() < 200 || response.code() >= 300) {
                throw new APIException("Could not create pipeline token. Due to: " + response.body().string(), response.code());
            }

            TokenResponse tokenResponse = GsonTransformer.fromJson(response.body().string(), TokenResponse.class);
            return tokenResponse.getAuth().getClientToken();
        } catch (IOException e) {
            throw new APIException(e);
        }
    }

    protected String oidcToken(String pipelineAuthToken, String path) throws APIException {

        Request request = new Request.Builder()
                .header(X_VAULT_TOKEN, pipelineAuthToken)
                .url(vaultConfig.getAddress() + path)
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();

            if (response.code() < 200 || response.code() >= 300) {
                throw new APIException("Could not read OIDC token. Due to: " + response.body().string(), response.code());
            }

            Type type = new TypeToken<DataResponse<OICDTokenResponse>>() {}.getType();
            DataResponse<OICDTokenResponse> dataResponse = GsonTransformer.fromJson(response.body().string(), type);
            return dataResponse.getData().getToken();
        } catch (IOException e) {
            throw new APIException(e);
        }
    }

    private String entityAliasName(String pipelineName) {
        return String.format("gocd-pipeline-dev-test-%s", pipelineName.toLowerCase().replaceAll("\\s+", "-"));
    }
}
