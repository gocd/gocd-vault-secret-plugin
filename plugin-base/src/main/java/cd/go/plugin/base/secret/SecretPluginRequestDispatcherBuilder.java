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

package cd.go.plugin.base.secret;

import cd.go.plugin.base.RequestDispatcherBuilder;
import cd.go.plugin.base.executors.IconRequestExecutor;
import cd.go.plugin.base.executors.MetadataExecutor;
import cd.go.plugin.base.executors.ValidationExecutor;
import cd.go.plugin.base.executors.ViewRequestExecutor;
import cd.go.plugin.base.validation.Validator;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;

public class SecretPluginRequestDispatcherBuilder extends RequestDispatcherBuilder<SecretPluginRequestDispatcherBuilder> {
    private static final String REQUEST_SECRETS_LOOKUP = "go.cd.secrets.secrets-lookup";
    private static final String REQUEST_GET_CONFIG_METADATA = "go.cd.secrets.secrets-config.get-metadata";
    private static final String REQUEST_GET_CONFIG_VIEW = "go.cd.secrets.secrets-config.get-view";
    private static final String REQUEST_VALIDATE_CONFIG = "go.cd.secrets.secrets-config.validate";
    private static final String REQUEST_VERIFY_CONNECTION = "go.cd.secrets.secrets-config.verify-connection";

    public SecretPluginRequestDispatcherBuilder(GoApplicationAccessor accessor) {
        super("secrets", accessor);
    }

    public SecretPluginRequestDispatcherBuilder icon(String path, String contentType) {
        return register(REQUEST_GET_ICON, new IconRequestExecutor(path, contentType));
    }

    public SecretPluginRequestDispatcherBuilder configMetadata(Class<?> configClass) {
        return register(REQUEST_GET_CONFIG_METADATA, new MetadataExecutor("", configClass));
    }

    public SecretPluginRequestDispatcherBuilder configView(String path) {
        return register(REQUEST_GET_CONFIG_VIEW, new ViewRequestExecutor(path));
    }

    public SecretPluginRequestDispatcherBuilder validateSecretConfig(Validator... validators) {
        return register(REQUEST_VALIDATE_CONFIG, new ValidationExecutor(validators));
    }

    public SecretPluginRequestDispatcherBuilder lookup(LookupExecutor executor) {
        return register(REQUEST_SECRETS_LOOKUP, executor);
    }
}
