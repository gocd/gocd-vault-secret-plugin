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
import io.github.jopenlibs.vault.VaultException;
import io.github.jopenlibs.vault.api.Auth;
import io.github.jopenlibs.vault.response.AuthResponse;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CertAuthenticatorTest {
    @Nested
    class authenticate {
        @Test
        void shouldAuthenticateWithVault() throws VaultException {
            Map<String, String> secretConfigMap = new HashMap<>();
            secretConfigMap.put("AuthMethod", "cert");
            SecretConfig secretConfig = SecretConfig.fromJSON(secretConfigMap);
            Vault vault = mock(Vault.class);
            Auth auth = mock(Auth.class);
            AuthResponse authResponse = mock(AuthResponse.class);

            when(vault.auth()).thenReturn(auth);
            when(auth.loginByCert()).thenReturn(authResponse);
            when(authResponse.getAuthClientToken()).thenReturn("auth_token");

            String token = new CertAuthenticator().authenticate(vault, secretConfig);

            assertThat(token).isEqualTo("auth_token");
        }
    }
}