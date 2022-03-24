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

public class OICDTokenResponse {


    @Expose
    @SerializedName("client_id")
    private String clientId;

    @Expose
    @SerializedName("token")
    private String token;

    @Expose
    @SerializedName("ttl")
    private long ttl;

    public OICDTokenResponse() {
    }

    public String getClientId() {
        return clientId;
    }

    public String getToken() {
        return token;
    }

    public long getTtl() {
        return ttl;
    }
}
