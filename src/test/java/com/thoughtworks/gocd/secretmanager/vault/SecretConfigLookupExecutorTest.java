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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@MockitoSettings
class SecretConfigLookupExecutorTest {
    public static final String VAULT_ROOT = "secret/gocd";
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private VaultProvider vaultProvider;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Logical logical;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SecretConfigRequest request;

    @BeforeEach
    void setUp() throws VaultException {
        when(request.getConfiguration().getVaultPath()).thenReturn(VAULT_ROOT);
        when(vaultProvider.vaultFor(any()).logical()).thenReturn(logical);
    }

    @Test
    void shouldReturnLookupResponse() throws Exception {
        when(logical.read(VAULT_ROOT).getData()).thenReturn(Map.of(
                "key1", "secret1",
                "key2", "secret2",
                "a:b:c", "secret3",
                "a:b:c/a:b:c", "secret4"));
        when(logical.read(VAULT_ROOT + "/a").getData()).thenReturn(Map.of(
                "key1", "secret1@a",
                "key2", "secret2@a"));
        when(logical.read(VAULT_ROOT + "/a/b/c").getData()).thenReturn(Map.of(
                "key1", "secret1@a/b/c",
                "key2", "secret2@a/b/c"));
        when(logical.read(VAULT_ROOT + "/a:b:c").getData()).thenReturn(Map.of(
                "a:b:c", "secret1@a:b:c"));

        Map<String, String> requestToExpectedResult = new LinkedHashMap<>() {{
            put("key1", "secret1");
            put("key2", "secret2");
            put("key3", "");
            put("/key1", ""); // Slash not treated as part of path, as no key delimiter
            put(":key1", "secret1");
            put("/:key2", "secret2");
            put("a:key1", "secret1@a");
            put("/a/b/c:key2", "secret2@a/b/c");
            put("/a/b/c/a:b:key", ""); // Should be treated just as key lookup for entire string
            put("a:b:c", "secret3"); // Should be treated just as key lookup for entire string
            put("a:b:c/a:b:c", "secret4"); // Should be treated just as key lookup for entire string
        }};

        when(request.getKeys()).thenReturn(new ArrayList<>(requestToExpectedResult.keySet()));

        final GoPluginApiResponse response = new SecretConfigLookupExecutor(vaultProvider)
                .execute(request);

        assertThat(response.responseCode()).isEqualTo(200);
        final String expectedResponse = requestToExpectedResult.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .map(e -> "  {\n" +
                        "    \"key\": \"" + e.getKey() + "\",\n" +
                        "    \"value\": \"" + e.getValue() + "\"\n" +
                        "  }")
                .collect(Collectors.joining(",\n", "[\n", "]"));

        assertEquals(expectedResponse, response.responseBody(), true);

        verify(logical, times(2)).read(VAULT_ROOT);
        verify(logical, times(2)).read(VAULT_ROOT + "/a");
        verify(logical, times(2)).read(VAULT_ROOT + "/a/b/c");
        verify(logical).read(VAULT_ROOT + "/a:b:c");

        verifyNoMoreInteractions(logical);
    }

    @Test
    void shouldErrorForInvalidPath() throws VaultException {
        when(logical.read(VAULT_ROOT + "/notExists")).thenThrow(VaultException.class);
        when(request.getKeys()).thenReturn(List.of("notExists:secret"));

        final GoPluginApiResponse response = new SecretConfigLookupExecutor(vaultProvider)
                .execute(request);

        assertThat(response.responseCode()).isEqualTo(500);
    }
}