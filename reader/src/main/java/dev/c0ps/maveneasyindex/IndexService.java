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

import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.c0ps.commons.ResourceUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public class IndexService {

    private static final Logger LOG = LoggerFactory.getLogger(IndexService.class);

    private final RepositoryUtils r;
    private final IndexFileReader reader;
    private final String indexContent;

    private final Map<Integer, Set<Artifact>> cache = new HashMap<>();

    @Inject
    public IndexService(RepositoryUtils r, IndexFileReader reader) {
        this.r = r;
        this.reader = reader;
        indexContent = ResourceUtils.readResourceToString("index.html", UTF_8);
    }

    @GET
    public Response getIndex() throws IOException {
        LOG.info("Request to index ...");
        return Response.ok(indexContent).build();
    }

    @GET
    @Path("/exists/{num}")
    public Response exists(@PathParam("num") int num) {
        LOG.info("Requesting existence for {} ...", num);
        return num > 0 && r.exists(num) //
                ? Response.ok().build() //
                : Response.status(NOT_FOUND).build();
    }

    @GET
    @Path("/get/{num}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("num") int num) {
        LOG.info("Returning artifacts for {} ...", num);
        return num > 0 && r.exists(num) //
                ? Response.ok(getArtifacts(num)).build()
                : Response.status(NOT_FOUND).build();
    }

    private Set<Artifact> getArtifacts(int num) {
        if (cache.containsKey(num)) {
            LOG.info("Memory cache hit for index #{}", num);
            return cache.get(num);
        }
        var file = r.download(num);
        var artifacts = reader.readIndexFile(file);
        cache.put(num, artifacts);
        return artifacts;
    }
}