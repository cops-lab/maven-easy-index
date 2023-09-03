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

import com.beust.jcommander.Parameter;

public class Args {

    @Parameter(names = "--baseFolder", arity = 1, description = "Folder for index files")
    public String baseFolder;

    @Parameter(names = "--url", arity = 1, description = "URL pattern for the index files")
    public String indexUrl = "https://repo1.maven.org/maven2/.index/nexus-maven-repository-index.%d.gz";
}