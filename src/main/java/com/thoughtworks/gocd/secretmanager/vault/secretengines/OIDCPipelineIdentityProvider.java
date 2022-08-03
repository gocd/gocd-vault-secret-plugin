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

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.thoughtworks.gocd.secretmanager.vault.api.GoCDPipelineApi;
import com.thoughtworks.gocd.secretmanager.vault.api.VaultApi;
import com.thoughtworks.gocd.secretmanager.vault.http.exceptions.APIException;
import com.thoughtworks.gocd.secretmanager.vault.models.PipelineMaterial;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;

import java.util.Optional;

public class OIDCPipelineIdentityProvider extends SecretEngine {

    private final GoCDPipelineApi gocd;
    private final VaultApi vault;
    private SecretConfig secretConfig;

    public OIDCPipelineIdentityProvider(Vault vault, VaultConfig vaultConfig, SecretConfig secretConfig) {
        super(vault);
        this.secretConfig = secretConfig;
        this.gocd = new GoCDPipelineApi(secretConfig);
        this.vault = new VaultApi(vaultConfig, secretConfig);
    }

    // Test usage
    public OIDCPipelineIdentityProvider(Vault vault, SecretConfig secretConfig, GoCDPipelineApi gocd, VaultApi vaultAPI) {
        super(vault);
        this.secretConfig = secretConfig;
        this.gocd = gocd;
        this.vault = vaultAPI;
    }


    @Override
    public Optional<String> getSecret(String path, String pipelineName) throws APIException {
        PipelineMaterial pipelineMaterial = gocd.fetchPipelineMaterial(pipelineName);

        String entityId = vault.identity().createPipelineEntity(entityName(pipelineName), secretConfig.getPipelinePolicy(), pipelineMaterial)
                .orElse(vault.identity().fetchPipelineEntity(entityName(pipelineName)))
                .getData()
                .getId();

        String authMountAccessor = vault.sys().getAuthMountAccessor();
        vault.identity().createPipelineEntityAlias(entityId, authMountAccessor, entityAliasName(pipelineName));

        String pipelineAuthToken = vault.auth().assumePipeline(secretConfig.getPipelineTokenAuthBackendRole(), secretConfig.getPipelinePolicy(), entityAliasName(pipelineName));
        return Optional.of(vault.identity().oidcToken(pipelineAuthToken, path));
    }

    private String entityAliasName(String pipelineName) {
        return entityName(pipelineName, "-entity-alias");
    }

    private String entityName(String pipelineName) {
        return entityName(pipelineName, "");
    }

    private String entityName(String pipelineName, String additionalPrefix) {
        return String.format("%s%s-%s",
                secretConfig.getCustomEntityNamePrefix(),
                additionalPrefix,
                pipelineName.toLowerCase().replaceAll("\\s+", "-")
        );
    }
}
