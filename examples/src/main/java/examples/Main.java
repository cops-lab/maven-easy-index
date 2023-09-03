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
package examples;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import org.glassfish.jersey.client.ClientConfig;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.c0ps.maveneasyindex.Artifact;
import dev.c0ps.maveneasyindex.ArtifactModule;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ContextResolver;

public class Main {

    private static final String SERVER = "http://localhost:8080";
    private static final String BASE_PATH = "/";

    private static Client client;

    public static void main(String[] args) {

        setupClient();

        // check whether an index id exists
        if (exist(456)) {
            // open the index file ...
            get(456).forEach(a -> {
                // ... and process each artifact
            });
        }
    }

    private static void setupClient() {
        // setup the Jackson ObjectMapper ...
        var om = new ObjectMapper()
                // ... register the ArtifactModule;
                .registerModule(new ArtifactModule());

        // let Jersey pick up the ObjectMapper through injection
        var config = new ClientConfig().register(new ContextResolver<ObjectMapper>() {
            @Override
            public ObjectMapper getContext(Class<?> type) {
                return om;
            }
        });

        // construct the client
        client = ClientBuilder.newClient(config);
    }

    // construct a request, existence is defined by status code (200 or 404)
    private static boolean exist(int i) {
        var r = client //
                .target(SERVER).path(BASE_PATH).path("/exists/" + i) //
                .request(APPLICATION_JSON) //
                .get();
        int s = r.getStatus();
        int t = Status.OK.getStatusCode();
        return s == t;
    }

    // construct a request, note that the type of artifacts is List<Artifact>
    private static List<Artifact> get(int i) {
        return client.target(SERVER).path(BASE_PATH).path("/get/" + i) //
                .request(APPLICATION_JSON) //
                .get(new GenericType<List<Artifact>>() {});

    }
}