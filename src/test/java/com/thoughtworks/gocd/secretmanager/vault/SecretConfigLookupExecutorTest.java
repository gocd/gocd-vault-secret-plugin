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

package com.thoughtworks.gocd.secretmanager.vault;

import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultException;
import com.bettercloud.vault.api.Logical;
import com.bettercloud.vault.response.LogicalResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import com.thoughtworks.gocd.secretmanager.vault.request.SecretConfigRequest;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class SecretConfigLookupExecutorTest {
    @Mock
    private ClientFactory clientFactory;
    @Mock
    private Vault vault;
    @Mock
    private Logical logical;

    @BeforeEach
    void setUp() throws VaultException {
        initMocks(this);
        when(clientFactory.create(any())).thenReturn(vault);
        when(vault.logical()).thenReturn(logical);
    }

    @Test
    void shouldReturnLookupResponse() throws VaultException, JSONException {
        final LogicalResponse logicalResponse = mock(LogicalResponse.class);
        final SecretConfigRequest request = mock(SecretConfigRequest.class);
        when(logical.read(anyString())).thenReturn(logicalResponse);
        when(logicalResponse.getData()).thenReturn(new HashMap<String, String>() {{
            put("AWS_ACCESS_KEY", "ASKDMDASDKLASDI");
            put("AWS_SECRET_KEY", "slfjskldfjsdjflfsdfsffdadsdfsdfsdfsd;");
        }});
        when(request.getConfiguration()).thenReturn(mock(SecretConfig.class));
        when(request.getKeys()).thenReturn(Arrays.asList("AWS_ACCESS_KEY", "AWS_SECRET_KEY"));

        final GoPluginApiResponse response = new SecretConfigLookupExecutor(clientFactory)
                .execute(request);

        assertThat(response.responseCode()).isEqualTo(200);
        final String expectedResponse = "[\n" +
                "  {\n" +
                "    \"key\": \"AWS_ACCESS_KEY\",\n" +
                "    \"value\": \"ASKDMDASDKLASDI\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"AWS_SECRET_KEY\",\n" +
                "    \"value\": \"slfjskldfjsdjflfsdfsffdadsdfsdfsdfsd;\"\n" +
                "  }\n" +
                "]";
        assertEquals(expectedResponse, response.responseBody(), true);
    }
}