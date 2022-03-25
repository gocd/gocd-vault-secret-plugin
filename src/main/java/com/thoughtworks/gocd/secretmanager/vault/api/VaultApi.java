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

import com.bettercloud.vault.VaultConfig;
import com.thoughtworks.gocd.secretmanager.vault.http.OkHTTPClientFactory;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import okhttp3.OkHttpClient;

public class VaultApi {

    private final VaultConfig vaultConfig;
    private final SecretConfig secretConfig;
    private final OkHttpClient vaultClient;

    public static final String X_VAULT_TOKEN = "X-Vault-Token";


    public VaultApi(VaultConfig vaultConfig, SecretConfig secretConfig) {
        this.vaultConfig = vaultConfig;
        this.secretConfig = secretConfig;
        this.vaultClient = new OkHTTPClientFactory().vault(secretConfig);
    }

    public VaultAuthApi auth() {
        return new VaultAuthApi(secretConfig, vaultConfig, vaultClient);
    }

    public VaultIdentityApi identity() {
        return new VaultIdentityApi(vaultConfig, vaultClient);
    }

    public VaultSysApi sys() {
        return new VaultSysApi(vaultConfig, vaultClient);
    }
}
