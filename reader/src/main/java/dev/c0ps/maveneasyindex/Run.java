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

import com.google.inject.Injector;

import dev.c0ps.libhttpd.HttpServerGracefulShutdownThread;
import dev.c0ps.libhttpd.HttpServerImpl;
import jakarta.inject.Inject;

public class Run implements Runnable {

    private Injector injector;

    @Inject
    public Run(Injector injector) {
        this.injector = injector;
    }

    @Override
    public void run() {
        var server = new HttpServerImpl(injector, 8080, "/");

        server.register(IndexService.class);

        Runtime.getRuntime().addShutdownHook(new HttpServerGracefulShutdownThread(server));
        server.start();
    }
}