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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.Map;

import static cd.go.plugin.base.ResourceReader.readResourceBytes;
import static java.util.Base64.getDecoder;
import static org.assertj.core.api.Assertions.assertThat;

class ViewRequestExecutorTest {

    @Test
    void shouldReturnViewWithTemplate() {
        final GoPluginApiResponse response = new ViewRequestExecutor("/dummy-template.html").execute(null);

        final Type type = new TypeToken<Map<String, String>>() {
        }.getType();

        assertThat(response.responseCode()).isEqualTo(200);

        Map<String, String> responseBody = new Gson().fromJson(response.responseBody(), type);
        assertThat(responseBody.size()).isEqualTo(1);
        assertThat(getDecoder().decode(responseBody.get("template"))).isEqualTo(readResourceBytes("/dummy-template.html"));
    }
}