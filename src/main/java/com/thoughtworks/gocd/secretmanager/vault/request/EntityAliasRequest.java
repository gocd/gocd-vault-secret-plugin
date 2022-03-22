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

package com.thoughtworks.gocd.secretmanager.vault.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;

import java.util.List;

public class EntityAliasRequest {

    @Expose
    @SerializedName("name")
    private String name;

    @Expose
    @SerializedName("canonical_id")
    private String canonicalId;

    @Expose
    @SerializedName("mount_accessor")
    private String mountAccessor;

    @Expose
    @SerializedName("custom_metadata")
    private CustomMetadataRequest customMetadata;

    public EntityAliasRequest(String name, String canonicalId, String mountAccessor, CustomMetadataRequest customMetadata) {
        this.name = name;
        this.canonicalId = canonicalId;
        this.mountAccessor = mountAccessor;
        this.customMetadata = customMetadata;
    }
}
