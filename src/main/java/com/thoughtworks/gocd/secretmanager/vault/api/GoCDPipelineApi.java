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
import com.thoughtworks.gocd.secretmanager.vault.http.OkHTTPClientFactory;
import com.thoughtworks.gocd.secretmanager.vault.http.exceptions.APIException;
import com.thoughtworks.gocd.secretmanager.vault.models.PipelineMaterial;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import com.thoughtworks.gocd.secretmanager.vault.request.gocd.PipelineConfigMaterialResponse;
import com.thoughtworks.gocd.secretmanager.vault.request.gocd.PipelineConfigResponse;
import com.thoughtworks.gocd.secretmanager.vault.request.gocd.SCMResponse;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class GoCDPipelineApi {

    private final OkHttpClient client;
    private final String gocdServerURL;

    public static final MediaType ACCEPT_GOCD_V11_JSON = MediaType.parse("application/vnd.go.cd.v11+json");
    public static final MediaType ACCEPT_GOCD_V4_JSON = MediaType.parse("application/vnd.go.cd.v4+json");

    public GoCDPipelineApi(SecretConfig secretConfig) {
        this.client = new OkHTTPClientFactory().gocd(secretConfig);
        this.gocdServerURL = secretConfig.getGocdServerURL();
    }

    public PipelineMaterial fetchPipelineMaterial(String pipeline) throws APIException {
        Request request = new Request.Builder()
                .url(gocdServerURL + "/go/api/admin/pipelines/" + pipeline)
                .header("Accept", ACCEPT_GOCD_V11_JSON.toString())
                .get()
                .build();
        try {
            Response response = client.newCall(request).execute();
            PipelineConfigResponse pipelineConfigResponse = GsonTransformer.fromJson(response.body().string(), PipelineConfigResponse.class);
            if (pipelineConfigResponse.getMaterials().isEmpty()) {
                throw new IllegalStateException(String.format("Material configuration for pipeline %s is empty. Can not infer material context.", pipeline));
            }

            if (response.code() < 200 || response.code() >= 300) {
                throw new APIException(String.format("Could not fetch pipeline configuration for pipeline %s. Due to: %s", pipeline, response.body().string()), response.code());
            }

            // Even if there are multiple pipeline definitions, we will always use the first found material.
            PipelineConfigMaterialResponse pipelineConfigMaterialResponse = pipelineConfigResponse.getMaterials().get(0);
            String materialType = pipelineConfigMaterialResponse.getType();
            String repositoryUrl = pipelineConfigMaterialResponse.getAttributes().getUrl();
            if(materialType.equalsIgnoreCase("plugin")) {
                repositoryUrl = fetchSCMRepositoryUrl(pipelineConfigResponse.getName());
            }

            return new PipelineMaterial(
                    pipelineConfigResponse.getName(),
                    pipelineConfigResponse.getGroup(),
                    pipelineConfigMaterialResponse.getAttributes().getBranch(),
                    repositoryUrl
            );
        } catch (IOException e) {
            throw new APIException(e);
        }
    }

    private String fetchSCMRepositoryUrl(String name) throws APIException {
        Request request = new Request.Builder()
                .url(gocdServerURL + "/go/api/admin/scms/" + name)
                .header("Accept", ACCEPT_GOCD_V4_JSON.toString())
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            SCMResponse pipelineConfigResponse = GsonTransformer.fromJson(response.body().string(), SCMResponse.class);

            if (response.code() < 200 || response.code() >= 300) {
                throw new APIException(String.format("Could not fetch pipeline configuration for pipeline %s. Due to: %s", name, response.body().string()), response.code());
            }

            if (pipelineConfigResponse.getConfigurations().isEmpty()) {
                throw new IllegalStateException(String.format("Material configuration for scm %s is empty. Can not infer material context.", name));
            }

            return pipelineConfigResponse.getConfigurations()
                    .stream()
                    .filter(scmConfiguration -> scmConfiguration.getKey().equalsIgnoreCase("url"))
                    .findFirst()
                    .map(scmConfiguration -> scmConfiguration.getValue())
                    .orElseThrow(() -> new IllegalStateException(String.format("Material configuration for scm %s does not contain repository url.", name)));
        } catch (IOException e) {
            throw new APIException(e);
        }


    }
}
