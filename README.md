# Maven Easy Index

This repository contains *Maven Easy Index*, a library and REST API that provides simplified access to the [Maven Central Index][index].
The [Maven Central Index][index] describes all changes in the repository, like added or deleted packages.
This index is updated regularly and incremental index files are released in consecutively numbered files.
This makes it convenient for automated tooling to keep track of the contents of the Maven Central repository.

Unfortunately, it is inconvenient to read these files from within an existing application.
The official documentation recommends to [convert these files][index-read] for easier processing.
While it *is* possible to read the index file from within an existing application, it has two major downsides.
1) The required setup is unnecessarily cumbersome.
Internally, [Lucene][lucene] is used for the indexing, however, the instantiation is not straight-forward and requires the introduction of various non-obvious dependencies to your project.
2) The index files need to stay backwards compatible, so the tooling around them is barely updated, including the required dependencies.
The index can only be read with certain dependency versions, so when these are added to an existing applications, special care is required to not break comatibility.
For example, the dependencies *require* an outdated [Guice][guice] version and will not work with the up-to-date release.
Unortuantely, these issues will only pop-up during runtime, so problems are hard to spot early on.

The *Maven Easy Index* solves these issues.
It provides a library that can directly open the index files.
However, instead of integrating the library into your application, the idea is to isolate all these issues in a container and to expose the relevant information via a REST API.
To further simplify the integration, we provide a data sructure and a corresponding [Jackson Module][jackson] that can handle the deserialization in API requests.

In short, the *Maven Easy Index* has the following features:

- Fragile dependency construct is isolated into a container.
- REST API provides easy access to the index contents.
- Providing data structures and serialization modules makes it trivial to use the REST API.
- Downloaded index files can be cached to prevent re-download.
- Building block for extending an existing micro-service architecture.
- Container and libraries are available through the GitHub package registry.


[central]: https://repo1.maven.org/maven2/
[index]: https://repo1.maven.org/maven2/.index/
[index-read]: https://maven.apache.org/repository/central-index.html
[guice]: https://github.com/google/guice
[jackson]: https://github.com/FasterXML/jackson
[jersey]: https://eclipse-ee4j.github.io/jersey/
[ghcr]: https://ghcr.io/
[lucene]: https://lucene.apache.org/

**Please note:** Packages of *Maven Easy Index* are released in the [COPS Lab Packages Repository](https://github.com/cops-lab/packages) and can be added to your project as a regular Maven dependency. Refer to the GitHub documentation to understand [how to add that repository as a package registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry) to a Maven `pom.xml` file.

#### Start the REST API

All that is necessary to get started is to authenticate with the [GitHub container registry][ghcr].
After that, the container can directly be started.
At the very minimum, it is required to expose the internal HTTP server, in the example, `-p 8080:8080` results in the server being accessible via [localhost:8080](http://localhost:8080/).

    $ docker login ghcr.io
    ...
    $ docker run --rm -p 8080:8080 ghcr.io/cops-lab/maven-easy-index:0.0.5

By default, *Maven Easy Index* will just store the downloaded index files in the container.
To cache the index files and prevent repeated downloads, once can mount a local folder (e.g., `/your/folder`) into the container (i.e., `/cache`).

    $ docker run --rm -p 8080:8080 -v /your/folder:/cache ghcr.io/cops-lab/maven-easy-index:0.0.5

Once the image has been downloaded and the server has been started, three endpoints can be used to conveniently access the index files by providing their corresponding index number.

##### :arrow_forward: GET /

The index file contains some basic instructions on how to use the REST API, similar to this document.


##### :arrow_forward: GET /exists/«int»

    200 (OK): the indicated index exists
    404 (NOT FOUND): the indicated index does not exist

Responses of this endpoint do not contain any meaningful message body, the relevant information is encoded in the HTTP header.
Existing packages will result in an HTTP status code of 200...

    $ curl -v localhost:8080/exists/457
    ...
    < HTTP/1.1 200 OK
    ...

... non-existing packages will result in an HTTP status code 404.

    $ curl -v localhost:8080/exists/1234
    ...
    < HTTP/1.1 404 Not Found
    ...

##### :arrow_forward: GET /get/«int»

    200 (OK): response contains a JSON array of all contained artifacts
    404 (NOT FOUND): the indicated index does not exist

Contents of index files can be requested with the `/get/` endpoint.
If a package is not available, the request will terminate with an HTTP status code of 404.
However, if a package can be found, the message body will contain a JSON array of contained Maven artifacts.

    $ curl localhost:8080/get/456
    [
        "club.zhcs:axe-validation:1.3:jar:1504455133497",
        "me.lyh:shapeless-datatype-datastore_1.3_2.12:0.1.7:jar:1504432224565",
        "io.openshift:booster-catalog-service:6:jar:1504433658050",
        "org.http4s:http4s-jetty_2.11:0.17.0-RC3:jar:1504415437787",
        "org.scalameta:transversers_2.12:2.0.0-RC1:jar:1504402068937",
        ...
    ]

The individual entries contain the following information, separated by `:`

- The coordinate, consisting of the `groupId`, `artifactId`, and `version`
- The `packaging` of the coordinate (e.g., `jar` or `pom`)
- The `release date` (in milliseconds)
- The `repository` is implicitely set to [Maven Central][central] when missing.

**Please Note:** If the `Artifact` class is used with repositories other than [Maven Central][central], the repository will be automatically included in the serialized JSON, e.g., `g:a:v:123@http://your.repo`.


#### Access Artifacts Programmatically

The *Maven Easy Index* makes it easy to consume the output, as long as you are building a Java program.
Once you add a dependency to `org.c0ps.maven-easy-index:data:0.0.5` to your project, you can reuse the data structure *Artifact* and the [Jackson Module][jackson] *ArtifactModule* to delegate serialization.
We recommend combining the module with a REST client like [Jersey][jersey], to be able to to conveniently read the responses without having to parse the JSON yourself.
The [examples](./examples/) contain a minimal client program that illustrates the required setup for succesfully communicating with the REST API.

If you want to avoid the microservice and open the index files directly in your own program, you can also just depend on `org.c0ps.maven-easy-index:reader:0.0.5` and use the  `RepositoryUtils` and `IndexFileReader` classes for downloading and reading.
Please note, however, that this has implications.
The index files requires specific dependency versions to work, so the overall construct will become more fragile and adds contraints to your dependencies.

It is recommended to use the REST API to access the index contents.




