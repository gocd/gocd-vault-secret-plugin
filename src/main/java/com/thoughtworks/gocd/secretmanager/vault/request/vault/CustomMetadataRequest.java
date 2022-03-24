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

package com.thoughtworks.gocd.secretmanager.vault.request.vault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.gocd.secretmanager.vault.models.PipelineMaterial;

public class CustomMetadataRequest {

    @Expose
    @SerializedName("group")
    private String group;

    @Expose
    @SerializedName("pipeline")
    private String pipeline;

    @Expose
    @SerializedName("repository")
    private String repository;

    @Expose
    @SerializedName("organization")
    private String organization;

    @Expose
    @SerializedName("branch")
    private String branch;

    public CustomMetadataRequest(String group, String pipeline, String repository, String organization, String branch) {
        this.group = group;
        this.pipeline = pipeline;
        this.repository = repository;
        this.organization = organization;
        this.branch = branch;
    }

    public CustomMetadataRequest(PipelineMaterial pipelineMaterial) {
        this(
                pipelineMaterial.getGroup(),
                pipelineMaterial.getName(),
                pipelineMaterial.getRepositoryName(),
                pipelineMaterial.getOrganization(),
                pipelineMaterial.getBranch()
        );
    }

    public String getPipeline() {
        return pipeline;
    }
}
