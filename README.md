# Deltaforge


**Important note:** Deltaforge is work in progress has not been used in a productive environment yet. File format compatibility may break, if any production critical error is found, but should remain stable otherwise.

Deltaforge is a tool to create and apply binary patches for application data (versions forward and _optional_ backward) based on the bsdiff algorithm. Deltaforge is the successor of [BiReUS](https://github.com/Brutus5000/BiReUS).

While the bsdiff algorthm itself can be applied on single files only, Deltaforge can take care on a whole set of file repositories.

Deltaforge covers the use case of patching data of which you do not own copyright. As such it is important that the patching works without ever distributing copyrighted files. The user itself has to provide the original data on his machine.

It aims to support the following use case:

*	You have a software (i.e. a computer game) where you have a set of assets (e.g. textures, source code).
  *	Binary files can also be zip-files which contain other text- or binary files.
*	You need to update the original assets on a regular base following a versioning convention.
*	The software produces save-files (i.e. replays) which can be viewed later. However, to correctly open the save-file you need to have the exact the exact state of plugins in their original version.
*	The patches are supposed to be stored on a server and retrieved on demand.
*	The software (client) handles the updates of the assets using delta-files generated on the server.


## Modules
This repository is composed of 4 gradle modules:
* **deltaforge-api-dto** contains all data transfer objects required to communicate with the server API.
* **deltaforge-client-starter** is a Spring Boot starter package / library which abstracts the whole Deltaforge client functionality into a single class.
* **deltaforge-patching** contains the patching logic of bsdiff4 applied on directories and archived files.
* **deltaforge-server** is a Spring Boot REST API offering information for clients and management endpoints to add / update existing repositories.


## Technologies used
* **Gradle** as build tool
* **Lombok** for less code overhead
* **MapStruct** for simplified mapping of internal objects and data transfer objects
* **Apache Commons IO and Compress** for handling (compressed) files
* **Jackson** for JSON (de-)serialization
* **jbsdiff** for patching file according to bsdiff4 (embedded since there is no working release available)
* **JGraphT** for resolving patch paths
* **Spring (Boot)** as a framework for client and server
* **Hibernate** as ORM (server only)
* **Elide** as a JSON-API compliant REST framework (server only)
* **jasminb jsonapi-converter** as a client library to deserialize JSON API (client only)