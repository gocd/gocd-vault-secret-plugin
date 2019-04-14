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

import cd.go.plugin.base.GsonTransformer;
import cd.go.plugin.base.secret.LookupExecutor;
import com.bettercloud.vault.Vault;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.secretmanager.vault.models.Secrets;
import com.thoughtworks.gocd.secretmanager.vault.request.SecretConfigRequest;

import java.util.Map;

class SecretConfigLookupExecutor extends LookupExecutor<SecretConfigRequest> {
    private final ClientFactory clientFactory;

    public SecretConfigLookupExecutor() {
        this(ClientFactory.instance());
    }

    SecretConfigLookupExecutor(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    protected GoPluginApiResponse execute(SecretConfigRequest request) {
        try {
            final Secrets secrets = new Secrets();
            final Vault vault = clientFactory.create(request.getConfiguration());

            final Map<String, String> secretsFromVault = vault.logical()
                    .read(request.getConfiguration().getBasePath())
                    .getData();

            for (String key : request.getKeys()) {
                if (secretsFromVault.containsKey(key)) {
                    secrets.add(key, secretsFromVault.get(key));
                }
            }

            return DefaultGoPluginApiResponse.success(GsonTransformer.toJson(secrets));
        } catch (Exception e) {
            LOGGER.error("Failed to lookup secret from vault.", e);
            return DefaultGoPluginApiResponse.error("Failed to lookup secrets form vault. See logs for more information.");
        }
    }

    @Override
    protected SecretConfigRequest parseRequest(String body) {
        return GsonTransformer.fromJson(body, SecretConfigRequest.class);
    }
}
