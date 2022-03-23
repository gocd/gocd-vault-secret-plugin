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

package com.thoughtworks.gocd.secretmanager.vault.http;

import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public class OkHTTPClientFactory {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public OkHttpClient buildFor(SecretConfig secretConfig) {
        // TODO: Handle SSL Config
        return new OkHttpClient.Builder()
                .readTimeout(secretConfig.getReadTimeout(), TimeUnit.SECONDS)
                .connectTimeout(secretConfig.getConnectionTimeout(), TimeUnit.SECONDS)
                .addInterceptor(new DefaultContentTypeInterceptor(JSON.toString()))
                .addInterceptor(new VaultHeaderInterceptor(secretConfig.getNameSpace()))
                .addInterceptor(new RetryInterceptor(secretConfig.getRetryIntervalMilliseconds(), secretConfig.getMaxRetries()))
                .build();
    }
}
