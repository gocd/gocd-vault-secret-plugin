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

package com.thoughtworks.gocd.secretmanager.vault.models;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

class SecretConfigTest {
    @Test
    void shouldReturnDefaultValueForTimeout() {
        HashMap<String, String> request = new HashMap<>();
        SecretConfig secretConfig = SecretConfig.fromJSON(request);
        assertThat(secretConfig.getConnectionTimeout()).isEqualTo(5);
        assertThat(secretConfig.getReadTimeout()).isEqualTo(30);
        assertThat(secretConfig.getMaxRetries()).isEqualTo(0);
        assertThat(secretConfig.getRetryIntervalMilliseconds()).isEqualTo(100);
    }

    @Test
    void shouldReturnTheValuesIfSet() {
        HashMap<String, String> request = new HashMap<>();
        request.put("ConnectionTimeout", "9");
        request.put("ReadTimeout", "50");
        request.put("MaxRetries", "5");
        request.put("RetryIntervalMilliseconds", "200");
        SecretConfig secretConfig = SecretConfig.fromJSON(request);
        assertThat(secretConfig.getConnectionTimeout()).isEqualTo(9);
        assertThat(secretConfig.getReadTimeout()).isEqualTo(50);
        assertThat(secretConfig.getMaxRetries()).isEqualTo(5);
        assertThat(secretConfig.getRetryIntervalMilliseconds()).isEqualTo(200);
    }
}