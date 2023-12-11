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

package com.thoughtworks.gocd.secretmanager.vault.authenticator;

import io.github.jopenlibs.vault.Vault;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class TokenAuthenticatorTest {
    @Nested
    class authenticate {
        @Test
        void shouldBeNoopsAndReturnTokenInSecretConfig() {
            Map<String, String> secretConfigMap = new HashMap<>();
            secretConfigMap.put("AuthMethod", "token");
            secretConfigMap.put("Token", "auth_token");
            SecretConfig secretConfig = SecretConfig.fromJSON(secretConfigMap);
            Vault vault = mock(Vault.class);

            String token = new TokenAuthenticator().authenticate(vault, secretConfig);

            assertThat(token).isEqualTo(secretConfig.getToken());
            verifyNoInteractions(vault);
        }
    }
}