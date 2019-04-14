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

import cd.go.plugin.base.annotations.Property;
import com.google.gson.annotations.Expose;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

class MetadataExecutorTest {

    @Test
    void shouldReturnMetadataFromClass() throws JSONException {
        final GoPluginApiResponse response = new MetadataExecutor("", FileConfig.class).execute(null);

        assertThat(response.responseCode()).isEqualTo(200);
        final String expected = "[\n" +
                "  {\n" +
                "    \"key\": \"path\",\n" +
                "    \"metadata\": {\n" +
                "      \"displayName\": \"\",\n" +
                "      \"required\": false,\n" +
                "      \"secure\": true\n" +
                "    }\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"isDirectory\",\n" +
                "    \"metadata\": {\n" +
                "      \"displayName\": \"\",\n" +
                "      \"required\": true,\n" +
                "      \"secure\": true\n" +
                "    }\n" +
                "  }\n" +
                "]";

        assertEquals(expected, response.responseBody(), true);
    }

    class FileConfig {
        @Expose
        @Property(name = "path", required = true)
        private String path;

        @Expose
        @Property(name = "isDirectory", required = true, secure = true)
        private boolean isDirectory;
    }
}