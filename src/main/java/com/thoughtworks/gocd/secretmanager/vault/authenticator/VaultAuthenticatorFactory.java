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

import java.util.Map;

public class VaultAuthenticatorFactory {
    private static final Map<String, VaultAuthenticator> authMethodToAuthenticator = Map.of(
            SecretConfig.TOKEN_AUTH_METHOD, new TokenAuthenticator(),
            SecretConfig.APPROLE_AUTH_METHOD, new AppRoleAuthenticator(),
            SecretConfig.CERT_AUTH_METHOD, new CertAuthenticator()
    );

    public VaultAuthenticator authenticatorFor(SecretConfig secretConfig) {
        return authMethodToAuthenticator.get(secretConfig.getAuthMethod());
    }
}
