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

package cd.go.plugin.base.executors;

import com.google.gson.JsonObject;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Optional;

import static cd.go.plugin.base.ResourceReader.readResourceBytes;
import static java.util.Base64.getEncoder;

public abstract class ResourceRequestExecutor implements Executor {
    private final String keyName;
    private final String resourcePath;

    ResourceRequestExecutor(String keyName, String resourcePath) {
        this.keyName = keyName;
        this.resourcePath = resourcePath;
    }

    @Override
    public GoPluginApiResponse execute(GoPluginApiRequest request) {
        JsonObject responseJson = new JsonObject();

        getContentType()
                .ifPresent(type -> responseJson.addProperty("content_type", type));

        responseJson.addProperty(keyName, getResourceAsEncodedString());
        return DefaultGoPluginApiResponse.success(GSON.toJson(responseJson));
    }

    private String getResourceAsEncodedString() {
        return getEncoder().encodeToString(readResourceBytes(resourcePath));
    }

    Optional<String> getContentType() {
        return Optional.empty();
    }
}
