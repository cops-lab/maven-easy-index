/*
 * Copyright 2022 Delft University of Technology
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

import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Provides;

import dev.c0ps.diapper.AssertArgs;
import dev.c0ps.diapper.InjectorConfig;
import dev.c0ps.diapper.InjectorConfigBase;
import jakarta.inject.Named;

@InjectorConfig
public class Config extends InjectorConfigBase {

    private Args args;

    public Config(Args args) {
        this.args = args;
    }

    @Provides
    public ObjectMapper provideObjectMapper() {
        return new ObjectMapper().registerModule(new ArtifactModule());
    }

    @Provides
    @Named("RepositoryUtils.indexUrl")
    public String provideIndexUrl() {
        AssertArgs.assertFor(args) //
                .notNull(a -> a.indexUrl, "index url cannot be null") //
                .that(a -> !a.indexUrl.isEmpty(), "index url cannot be empty") //
                .that(a -> a.indexUrl.contains("%d"), "index url must contain marker for index number");
        return args.indexUrl;
    }

    @Provides
    @Named("RepositoryUtils.baseFolder")
    public String provideBaseFolder() {
        AssertArgs.assertFor(args) //
                .notNull(a -> a.baseFolder, "must provide a folder") //
                .that(a -> new File(a.baseFolder).exists(), "folder must exist") //
                .that(a -> new File(a.baseFolder).isDirectory(), "folder is not a folder");
        return args.baseFolder;
    }
}