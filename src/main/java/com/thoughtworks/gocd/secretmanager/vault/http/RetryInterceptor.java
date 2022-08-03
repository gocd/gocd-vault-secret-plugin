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

import okhttp3.Interceptor;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RetryInterceptor implements Interceptor {
    private final int retryIntervalMilliseconds;
    private final int maxRetries;

    public RetryInterceptor(int retryIntervalMilliseconds, int maxRetries) {
        this.retryIntervalMilliseconds = retryIntervalMilliseconds;
        this.maxRetries = maxRetries;
    }

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        int retryCount = 0;
        while (true) {
            Response response = chain.proceed(chain.request());
            if (response.code() >= 400) {
                if (retryCount < maxRetries) {
                    retryCount++;
                    try {
                        Thread.sleep(retryIntervalMilliseconds);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    return response;
                }
            } else {
                return response;
            }
        }
    }
}
