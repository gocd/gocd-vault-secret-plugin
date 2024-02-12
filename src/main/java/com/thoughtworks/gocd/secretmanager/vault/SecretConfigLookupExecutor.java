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

import java.util.*;

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
            final List<Secret> secrets = new ArrayList<>();
            final Vault vault = vaultProvider.vaultFor(request.getConfiguration());

            final Map<String, Map<String, String>> vaultCache = new HashMap<>();
            for (String optionalPathKey : request.getKeys()) {
                PathKey resolved = PathKey.from(request.getConfiguration().getVaultPath(), optionalPathKey);

                Map<String, String> secretsFromVault = vaultCache.computeIfAbsent(resolved.path, p -> {
                    try {
                        LOGGER.info("Looking up secrets from vault [{}] at resolved path [{}]",
                                request.getConfiguration().getVaultUrl(), p);
                        return vault.logical().read(p).getData();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                Optional.ofNullable(secretsFromVault.get(resolved.key))
                        .ifPresentOrElse(
                                secret -> secrets.add(new Secret(optionalPathKey, secret)),
                                () -> LOGGER.warn("No secret value found in vault [{}] path [{}] for key [{}]",
                                        request.getConfiguration().getVaultUrl(), resolved.path, resolved.key)
                        );
            }

            return DefaultGoPluginApiResponse.success(toJson(secrets));
        } catch (Exception e) {
            LOGGER.error("Failed to lookup secret from vault.", e);
            return DefaultGoPluginApiResponse.error(toJson(singletonMap("message", "Failed to lookup secrets from vault. See logs for more information.")));
        }
    }

    private static class PathKey {
        String path;
        String key;
        PathKey(String path, String key) {
            this.path = path;
            this.key = key;
        }

        static PathKey from(String commonVaultPath, String optionalPathKey) {
            PathKey defaultPathKey = new PathKey(removeTrailingSlashes(commonVaultPath), optionalPathKey);
            String[] parts = optionalPathKey.split(":");

            // only `a:b` is treated specially, both `a` and `a:b:c:...` are treated as normal keys
            if (parts.length == 2) {
                // Remove leading /es from fhe first part
                String subPath = parts[0].replaceFirst("^/+", "");
                return subPath.isEmpty()
                    ? new PathKey(defaultPathKey.path, parts[1])
                    : new PathKey(defaultPathKey.path  + "/" + subPath, parts[1]);
            }

            return defaultPathKey;
        }

        private static String removeTrailingSlashes(String commonVaultPath) {
            return commonVaultPath.replaceFirst("/+$", "");
        }
    }

    @Override
    protected SecretConfigRequest parseRequest(String body) {
        return fromJson(body, SecretConfigRequest.class);
    }
}
