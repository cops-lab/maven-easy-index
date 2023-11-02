/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.c0ps.maveneasyindex;

import java.util.Date;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Artifact implements Cloneable {

    public static final String CENTRAL = "https://repo.maven.apache.org/maven2/";

    public String groupId;
    public String artifactId;
    public String version;
    public String packaging;

    public long releaseDate;
    public String repository = CENTRAL;

    public Artifact() {
        // required for serialization
    }

    public Artifact(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public Artifact(String groupId, String artifactId, String version, String packaging) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.packaging = packaging;
    }

    public Artifact setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate.getTime();
        return this;
    }

    public Artifact setReleaseDate(long releaseDate) {
        this.releaseDate = releaseDate;
        return this;
    }

    public Artifact setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    public boolean hasReleaseDate() {
        return releaseDate > 0;
    }

    @Override
    public Artifact clone() {
        var clone = new Artifact();
        clone.groupId = groupId;
        clone.artifactId = artifactId;
        clone.version = version;
        clone.packaging = packaging;
        clone.releaseDate = releaseDate;
        clone.repository = repository;
        return clone;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return new StringBuilder() //
                .append(groupId).append(':') //
                .append(artifactId).append(':') //
                .append(version).append(':') //
                .append(packaging).append(':') //
                .append(releaseDate).append('@') //
                .append(CENTRAL.equals(repository) ? "CENTRAL" : repository) //
                .toString();
    }
}