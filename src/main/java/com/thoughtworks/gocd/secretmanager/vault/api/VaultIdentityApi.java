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
import com.bettercloud.vault.VaultConfig;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.gocd.secretmanager.vault.http.exceptions.APIException;
import com.thoughtworks.gocd.secretmanager.vault.models.PipelineMaterial;
import com.thoughtworks.gocd.secretmanager.vault.request.vault.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

import static cd.go.plugin.base.GsonTransformer.toJson;
import static com.thoughtworks.gocd.secretmanager.vault.api.VaultApi.X_VAULT_TOKEN;
import static com.thoughtworks.gocd.secretmanager.vault.http.OkHTTPClientFactory.CONTENT_TYPE_JSON;

public class VaultIdentityApi {

    private final VaultConfig vaultConfig;
    private final OkHttpClient client;

    public VaultIdentityApi(VaultConfig vaultConfig, OkHttpClient client) {
        this.vaultConfig = vaultConfig;
        this.client = client;
    }

    public void createPipelineEntityAlias(String entityId, String mountAccessor, String entityAliasName) throws APIException {

        EntityAliasRequest entityAliasRequest = new EntityAliasRequest(
                entityAliasName,
                entityId,
                mountAccessor
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

    public String oidcToken(String pipelineAuthToken, String path) throws APIException {

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

    public Optional<EntityResponse> createPipelineEntity(String entityName, List<String> policies, PipelineMaterial pipelineMaterial) throws APIException {
        EntityRequest entityRequest = new EntityRequest(
                entityName,
                policies,
                new MetadataRequest(pipelineMaterial)
        );

        RequestBody body = RequestBody.create(toJson(entityRequest), CONTENT_TYPE_JSON);

        Request request = new Request.Builder()
                .header(X_VAULT_TOKEN, vaultConfig.getToken())
                .url(vaultConfig.getAddress() + "/v1/identity/entity/name/" + entityName)
                .post(body)
                .build();

        try {
            Response response = client.newCall(request).execute();

            if (response.code() < 200 || response.code() >= 300) {
                throw new APIException(String.format("Could not create entity [%s]. Due to: %s", entityName, response.body().string()), response.code());
            }

            if(response.code() == 200) {
                return Optional.of(GsonTransformer.fromJson(response.body().string(), EntityResponse.class));
            } else {
                // In case the entity already existed it was now updated
                return Optional.empty();
            }
        } catch (IOException e) {
            throw new APIException(e);
        }
    }

    public EntityResponse fetchPipelineEntity(String pipelineEntityName) throws APIException {
        Request request = new Request.Builder()
                .header(X_VAULT_TOKEN, vaultConfig.getToken())
                .url(vaultConfig.getAddress() + "/v1/identity/entity/name/" + pipelineEntityName)
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();

            if (response.code() < 200 || response.code() >= 300) {
                throw new APIException(String.format("Could not create entity [%s]. Due to: %s", pipelineEntityName, response.body().string()), response.code());
            }

            return GsonTransformer.fromJson(response.body().string(), EntityResponse.class);
        } catch (IOException e) {
            throw new APIException(e);
        }
    }
}
