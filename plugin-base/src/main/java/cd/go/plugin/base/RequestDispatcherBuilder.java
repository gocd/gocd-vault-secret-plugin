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

package cd.go.plugin.base;

import cd.go.plugin.base.executors.Executor;
import cd.go.plugin.base.secret.SecretPluginRequestDispatcherBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class RequestDispatcherBuilder<T extends RequestDispatcherBuilder> implements DispatcherBuilder {
    protected final Map<String, Executor> dispatcherRegistry = new HashMap<>();
    private final String extensionName;
    private final GoApplicationAccessor accessor;
    private GoPluginIdentifier pluginIdentifier;


    public RequestDispatcherBuilder(String extensionName, GoApplicationAccessor accessor) {
        this.extensionName = extensionName;
        this.accessor = accessor;
    }

    public static SecretPluginRequestDispatcherBuilder forSecret(GoApplicationAccessor accessor) {
        return new SecretPluginRequestDispatcherBuilder(accessor);
    }

    protected T register(String requestName, Executor executor) {
        dispatcherRegistry.put(requestName, executor);
        return (T) this;
    }

    public RequestDispatcher build() {
        return new RequestDispatcher(pluginIdentifier, dispatcherRegistry, accessor);
    }

    public T supportedVersions(String version, String... versions) {
        if (version == null || version.trim().isEmpty()) {
            throw new RuntimeException("Supported version(s) must not be blank.");
        }

        final List<String> allSupportedVersions = new ArrayList<>();
        allSupportedVersions.add(version);

        if (versions != null) {
            allSupportedVersions.addAll(Stream.of(versions).collect(Collectors.toSet()));
        }

        pluginIdentifier = new GoPluginIdentifier(extensionName, allSupportedVersions);
        return (T) this;
    }
}
