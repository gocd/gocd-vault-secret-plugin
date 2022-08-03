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

/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.thoughtworks.gocd.secretmanager.vault;

import cd.go.plugin.base.dispatcher.BaseBuilder;
import cd.go.plugin.base.dispatcher.RequestDispatcher;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.gocd.secretmanager.vault.models.SecretConfig;
import com.thoughtworks.gocd.secretmanager.vault.validation.*;

import static java.util.Collections.singletonList;

@Extension
public class VaultPlugin implements GoPlugin {
    private RequestDispatcher requestDispatcher;

    @Override
    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {
        requestDispatcher = BaseBuilder
                .forSecrets()
                .v1()
                .icon("/plugin-icon.png", "image/png")
                .configMetadata(SecretConfig.class)
                .configView("/secrets.template.html")
                .validateSecretConfig(new AuthMethodValidator(), new CertAuthMethodValidator(),
                        new AppRoleAuthMethodValidator(), new TokenAuthMethodValidator(),
                        new SecretEngineValidator(), new OIDCSecretEngineValidator())
                .lookup(new SecretConfigLookupExecutor())
                .build();
    }

    @Override
    public GoPluginApiResponse handle(GoPluginApiRequest request) {
        try {
            return requestDispatcher.dispatch(request);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("secrets", singletonList("1.0"));
    }
}
