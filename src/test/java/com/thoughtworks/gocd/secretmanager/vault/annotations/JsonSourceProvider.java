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

package com.thoughtworks.gocd.secretmanager.vault.annotations;

import cd.go.plugin.base.ResourceReader;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.util.Arrays;
import java.util.stream.Stream;

public class JsonSourceProvider implements ArgumentsProvider, AnnotationConsumer<JsonSource> {
    private String[] jsonFiles;

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Stream.of(Arguments.of(Arrays.stream(jsonFiles)
                .map(ResourceReader::readResource).toArray()));
    }

    @Override
    public void accept(JsonSource jsonSource) {
        jsonFiles = jsonSource.jsonFiles();
    }
}
