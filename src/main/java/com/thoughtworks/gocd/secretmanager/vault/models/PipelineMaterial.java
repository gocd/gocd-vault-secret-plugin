/*
 * Copyright 2022 ThoughtWorks, Inc.
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

package com.thoughtworks.gocd.secretmanager.vault.models;

import com.bettercloud.vault.VaultException;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PipelineMaterial {

    private final String name;
    private final String group;
    private final String organization;
    private final String repositoryName;
    // Nullable
    private final String branch;

    private final Pattern githubRepositoryURLRegex = Pattern.compile("git@github\\.com:(?<organization>[^\\/]+)\\/(?<repository>.+?)(\\.git)?", Pattern.CASE_INSENSITIVE);

    public PipelineMaterial(String name, String group, String organization, String repositoryName, String branch) {
        this.name = name;
        this.group = group;
        this.organization = organization;
        this.repositoryName = repositoryName;
        this.branch = branch;
    }

    public PipelineMaterial(String name, String group, String branch, String repositoryURL) {
        this.name = name;
        this.group = group;
        this.branch = branch;
        Matcher matcher = githubRepositoryURLRegex.matcher(repositoryURL);
        if (! matcher.matches()) {
            throw new IllegalStateException(String.format("Given URL [%s] is not a valid git ssh URL.", repositoryURL));
        }
        this.organization = matcher.group("organization");
        this.repositoryName = matcher.group("repository");
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public String getOrganization() {
        return organization;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getBranch() {
        return branch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PipelineMaterial that = (PipelineMaterial) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(group, that.group) &&
                Objects.equals(organization, that.organization) &&
                Objects.equals(repositoryName, that.repositoryName) &&
                Objects.equals(branch, that.branch);
    }
}
