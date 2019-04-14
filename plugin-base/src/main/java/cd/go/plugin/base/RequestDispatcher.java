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
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.Map;
import java.util.Optional;

public class RequestDispatcher {
    private final Map<String, Executor> dispatcherRegistry;
    private final GoApplicationAccessor accessor;

    RequestDispatcher(Map<String, Executor> dispatcherRegistry, GoApplicationAccessor accessor) {
        this.accessor = accessor;
        this.dispatcherRegistry = dispatcherRegistry;
    }


    public GoPluginApiResponse dispatch(GoPluginApiRequest request) throws UnhandledRequestTypeException {
        final Optional<Executor> executorOptional = Optional.ofNullable(dispatcherRegistry.get(request.requestName()));

        return executorOptional.map(executor -> executor.execute(request))
                .orElseThrow(() -> new UnhandledRequestTypeException(request.requestName()));
    }
}
