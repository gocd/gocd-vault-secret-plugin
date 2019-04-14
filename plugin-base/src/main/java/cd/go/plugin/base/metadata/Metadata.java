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

package cd.go.plugin.base.metadata;

import com.google.gson.annotations.Expose;

import java.util.Objects;

public class Metadata {
    @Expose
    private final String displayName;
    @Expose
    private final boolean required;
    @Expose
    private final boolean secure;

    public Metadata(String displayName, boolean required, boolean secure) {
        this.displayName = displayName;
        this.required = required;
        this.secure = secure;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isSecure() {
        return secure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Metadata)) return false;
        Metadata metadata = (Metadata) o;
        return required == metadata.required &&
                secure == metadata.secure &&
                Objects.equals(displayName, metadata.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(displayName, required, secure);
    }
}
