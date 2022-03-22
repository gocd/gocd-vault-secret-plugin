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
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Auth;
import com.bettercloud.vault.rest.Rest;
import com.bettercloud.vault.rest.RestException;
import com.bettercloud.vault.rest.RestResponse;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import com.thoughtworks.gocd.secretmanager.vault.request.CustomMetadataRequest;
import com.thoughtworks.gocd.secretmanager.vault.request.DataResponse;
import com.thoughtworks.gocd.secretmanager.vault.request.EntityAliasRequest;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cd.go.plugin.base.GsonTransformer.toJson;

public class OIDCPipelineIdentityProvider extends SecretEngine {

    private VaultConfig vaultConfig;
    private SecretConfig secretConfig;

    public OIDCPipelineIdentityProvider(Vault vault, VaultConfig vaultConfig, SecretConfig secretConfig) {
        super(vault);
        this.vaultConfig = vaultConfig;
        this.secretConfig = secretConfig;
    }

    @Override
    public Optional<String> getSecret(String path, String pipelineName) throws VaultException {
        CustomMetadataRequest customMetadataRequest = new CustomMetadataRequest(
                "some_group",
                pipelineName,
                "some_repository",
                "some_organization",
                "some_branch"
        );

        createPipelineEntityAlias(customMetadataRequest);

        String pipelineAuthToken = assumePipeline(customMetadataRequest.getPipeline());
        return Optional.of(oidcToken(pipelineAuthToken, path));
    }

    protected void createPipelineEntityAlias(CustomMetadataRequest customMetadataRequest) throws VaultException {
        EntityAliasRequest entityAliasRequest = new EntityAliasRequest(
                entityAliasName(customMetadataRequest.getPipeline()),
                "7b399f73-1547-9300-4a28-6e0d536571c4",
                "auth_token_40382420",
                customMetadataRequest
        );

        try {
            RestResponse restResponse = new Rest()
                    .url(vaultConfig.getAddress() + "/v1/identity/entity-alias")
                    .header("X-Vault-Token", vaultConfig.getToken())
                    .body(toJson(entityAliasRequest).getBytes(StandardCharsets.UTF_8))
                    .post();

            if (restResponse.getStatus() < 200 || restResponse.getStatus() >= 300) {
                String response = new String(restResponse.getBody(), StandardCharsets.UTF_8);
                throw new VaultException("Could not create entity alias. Due to: " + response, restResponse.getStatus());
            }

        } catch (RestException e) {
            throw new VaultException(e);
        }
    }

    protected String assumePipeline(String pipelineName) throws VaultException {

        // TODO: Fech these from secret COnfig
        Auth.TokenRequest tokenRequest = new Auth.TokenRequest()
                .entityAlias(entityAliasName(pipelineName))
                .role("gocd-pipeline-dev-test")
                .polices(Stream.of("gocd-pipeline-policy-dev-test").collect(Collectors.toList()));

        String pipelineAuthToken = getVault().auth().createToken(tokenRequest).getAuthClientToken();
        return pipelineAuthToken;
    }

    protected String oidcToken(String pipelineAuthToken, String path) throws VaultException {

        try {
            RestResponse restResponse = new Rest()
                    .url(vaultConfig.getAddress() + path)
                    .header("X-Vault-Token", pipelineAuthToken)
                    .get();

            String response = new String(restResponse.getBody(), StandardCharsets.UTF_8);
            if (restResponse.getStatus() != 200) {
                throw new VaultException("Could not read OIDC token. Due to: " + response, restResponse.getStatus());
            }

            DataResponse dataResponse = GsonTransformer.fromJson(response, DataResponse.class);
            return dataResponse.getData().getToken();

        } catch (RestException e) {
            throw new VaultException(e);
        }
    }

    private String entityAliasName(String pipelineName) {
        return String.format("gocd-pipeline-dev-test-%s", pipelineName.toLowerCase().replaceAll("\\s+", "-"));
    }
}
