/*
 * Copyright 2023 ThoughtWorks, Inc.
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
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.secretmanager.vault.models.Secret;
import com.thoughtworks.gocd.secretmanager.vault.request.SecretConfigRequest;
import io.github.jopenlibs.vault.Vault;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            final Map<String, Map<String, String>> vaultCache = new HashMap<>();
            final List<Secret> secrets = new ArrayList<>();
            final Vault vault = vaultProvider.vaultFor(request.getConfiguration());

            for (String pathKey : request.getKeys()) {
                String[] parts = pathKey.split(":", -1);
                String path = request.getConfiguration().getVaultPath().replaceFirst("/+$", "");
                String key;
                if (parts.length == 2) {
                    String subPath = parts[0].replaceFirst("^/+", "");
                    if (subPath.length() > 0)
                        path += "/" + subPath;
                    key = parts[1];
                }
                else { // only `a:b` is treated specially, both `a` and `a:b:c:...` are treated as normal keys
                    key = pathKey;
                }

                Map<String, String> secretsFromVault = vaultCache.get(path);
                if (secretsFromVault == null) {
                    secretsFromVault = vault.logical()
                                            .read(path)
                                            .getData();
                    vaultCache.put(path, secretsFromVault);
                }
                if (secretsFromVault.containsKey(key)) {
                    secrets.add(new Secret(pathKey, secretsFromVault.get(key)));
                }
            }

            return DefaultGoPluginApiResponse.success(toJson(secrets));
        } catch (Exception e) {
            LOGGER.error("Failed to lookup secret from vault.", e);
            return DefaultGoPluginApiResponse.error(toJson(singletonMap("message", "Failed to lookup secrets from vault. See logs for more information.")));
        }
    }

    @Override
    protected SecretConfigRequest parseRequest(String body) {
        return fromJson(body, SecretConfigRequest.class);
    }
}
