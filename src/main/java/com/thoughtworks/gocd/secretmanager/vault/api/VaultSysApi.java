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
import com.thoughtworks.gocd.secretmanager.vault.http.exceptions.APIException;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import com.thoughtworks.gocd.secretmanager.vault.request.vault.AuthMountsResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

import static com.thoughtworks.gocd.secretmanager.vault.api.VaultApi.X_VAULT_TOKEN;

public class VaultSysApi {

    private final VaultConfig vaultConfig;
    private final OkHttpClient client;

    public VaultSysApi(VaultConfig vaultConfig, OkHttpClient client) {
        this.vaultConfig = vaultConfig;
        this.client = client;
    }

    public String getAuthMountAccessor() throws APIException {
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
}
