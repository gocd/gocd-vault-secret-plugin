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

import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

class VaultAuthenticatorFactoryTest {
    @Nested
    class authenticatorFor {
        @Test
        void shouldReturnTokenVaultAuthenticator_forTokenAuthMethod() {
            SecretConfig secretConfig = SecretConfig.fromJSON(singletonMap("AuthMethod", "token"));

            VaultAuthenticator authenticator = new VaultAuthenticatorFactory().authenticatorFor(secretConfig);

            assertThat(authenticator).isInstanceOf(TokenAuthenticator.class);
        }

        @Test
        void shouldReturnCertVaultAuthenticator_forCertAuthMethod() {
            SecretConfig secretConfig = SecretConfig.fromJSON(singletonMap("AuthMethod", "cert"));

            VaultAuthenticator authenticator = new VaultAuthenticatorFactory().authenticatorFor(secretConfig);

            assertThat(authenticator).isInstanceOf(CertAuthenticator.class);
        }

        @Test
        void shouldReturnAppRoleVaultAuthenticator_forAppRoleAuthMethod() {
            SecretConfig secretConfig = SecretConfig.fromJSON(singletonMap("AuthMethod", "approle"));

            VaultAuthenticator authenticator = new VaultAuthenticatorFactory().authenticatorFor(secretConfig);

            assertThat(authenticator).isInstanceOf(AppRoleAuthenticator.class);
        }
    }

}