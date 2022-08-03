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

package com.thoughtworks.gocd.secretmanager.vault.request.gocd;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PipelineConfigMaterialAttributesResponse {

    // set if material type is git
    @Expose
    @SerializedName("url")
    private String url;
    @Expose
    @SerializedName("branch")
    private String branch;

    // set if pipeline type is plugin
    @Expose
    @SerializedName("ref")
    private String ref;

    // set if material type is dependency
    @Expose
    @SerializedName("pipeline")
    private String pipeline;

    public PipelineConfigMaterialAttributesResponse() {
    }

    public String getUrl() {
        return url;
    }

    public String getBranch() {
        return branch;
    }

    public String getRef() {
        return ref;
    }

    public String getPipeline() {
        return pipeline;
    }
}