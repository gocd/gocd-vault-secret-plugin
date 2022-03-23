/*
 * Copyright 2019 ThoughtWorks, Inc.
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

package com.thoughtworks.gocd.secretmanager.vault;

import cd.go.plugin.base.executors.secrets.LookupExecutor;
import com.bettercloud.vault.Vault;

import com.bettercloud.vault.VaultConfig;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import com.thoughtworks.gocd.secretmanager.vault.models.Secrets;
import com.thoughtworks.gocd.secretmanager.vault.request.SecretConfigRequest;
import com.thoughtworks.gocd.secretmanager.vault.secretengines.SecretEngine;
import com.thoughtworks.gocd.secretmanager.vault.builders.SecretEngineBuilder;

import static cd.go.plugin.base.GsonTransformer.fromJson;
import static cd.go.plugin.base.GsonTransformer.toJson;
import static java.util.Collections.singletonMap;

class SecretConfigLookupExecutor extends LookupExecutor<SecretConfigRequest> {
    private static final Logger LOGGER = Logger.getLoggerFor(SecretConfigLookupExecutor.class);
    private final VaultProvider vaultProvider;

    public SecretConfigLookupExecutor() {
        this(new VaultProvider());
    }

    SecretConfigLookupExecutor(VaultProvider vaultProvider) {
        this.vaultProvider = vaultProvider;
    }

    @Override
    protected GoPluginApiResponse execute(SecretConfigRequest request) {
        try {
            final Vault vault = vaultProvider.vaultFor(request.getConfiguration());
            final Secrets secrets = new Secrets();
            final String vaultPath = request.getConfiguration().getVaultPath();

            SecretEngine secretEngine = buildSecretEngine(request, vault, vaultProvider.getVaultConfig());

            for (String key : request.getKeys()) {
                secretEngine.getSecret(vaultPath, key).ifPresent(secret -> secrets.add(key, secret));
            }

            return DefaultGoPluginApiResponse.success(toJson(secrets));
        } catch (Exception e) {
            LOGGER.error("Failed to lookup secret from vault.", e);
            return DefaultGoPluginApiResponse.error(toJson(singletonMap("message", "Failed to lookup secrets from vault. See logs for more information.")));
        }
    }

    protected SecretEngine buildSecretEngine(SecretConfigRequest request, Vault vault, VaultConfig vaultConfig) {
        return new SecretEngineBuilder()
                .secretConfig(request.getConfiguration())
                .vault(vault)
                .vaultConfig(vaultConfig)
                .build();
    }

    @Override
    protected SecretConfigRequest parseRequest(String body) {
        return fromJson(body, SecretConfigRequest.class);
    }
}
