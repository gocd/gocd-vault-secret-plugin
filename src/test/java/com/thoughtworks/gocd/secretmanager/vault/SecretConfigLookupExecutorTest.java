/*
 * Copyright 2023 ThoughtWorks, Inc.
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

package com.thoughtworks.gocd.secretmanager.vault;

import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.secretmanager.vault.request.SecretConfigRequest;
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.api.Logical;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@MockitoSettings
class SecretConfigLookupExecutorTest {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private VaultProvider vaultProvider;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Logical logical;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SecretConfigRequest request;

    @BeforeEach
    void setUp() throws VaultException {
        when(vaultProvider.vaultFor(any()).logical()).thenReturn(logical);
        when(logical.read("secret/gocd").getData()).thenReturn(Map.of("key1", "secret1", "key2", "secret2"));
        when(logical.read("secret/gocd/a").getData()).thenReturn(Map.of("key1", "secret1@a", "key2", "secret2@a"));
        when(logical.read("secret/gocd/a/b/c").getData()).thenReturn(Map.of("key1", "secret1@a/b/c", "key2", "secret2@a/b/c"));
        when(logical.read("secret/gocd/notExists")).thenThrow(VaultException.class);
        when(request.getConfiguration().getVaultPath()).thenReturn("secret/gocd");
    }

    @Test
    void shouldReturnLookupResponse() throws JSONException {
        final List<String> requests = Arrays.asList("key1", "key2", "key3", ":key1", "/:key2", "a:key1", "/a/b/c:key2", "a:b:c");
        final List<String> secrets = Arrays.asList("secret1", "secret2", null, "secret1", "secret2", "secret1@a", "secret2@a/b/c", null);
        assertThat(requests.size()).isEqualTo(secrets.size());
        when(request.getKeys()).thenReturn(requests);

        final GoPluginApiResponse response = new SecretConfigLookupExecutor(vaultProvider)
                .execute(request);

        assertThat(response.responseCode()).isEqualTo(200);
        final String expectedResponse = IntStream.range(0, requests.size())
                                                 .filter(i -> secrets.get(i) != null)
                                                 .mapToObj(i -> "  {\n" +
                                                                "    \"key\": \"" + requests.get(i) + "\",\n" +
                                                                "    \"value\": \"" + secrets.get(i) + "\"\n" +
                                                                "  }")
                                                 .collect(Collectors.joining(",\n", "[\n", "]"));

        assertEquals(expectedResponse, response.responseBody(), true);
    }

    @Test
    void shouldErrorForInvalidPath() {
        when(request.getKeys()).thenReturn(List.of("notExists:secret"));

        final GoPluginApiResponse response = new SecretConfigLookupExecutor(vaultProvider)
                                                     .execute(request);

        assertThat(response.responseCode()).isEqualTo(500);
    }
}