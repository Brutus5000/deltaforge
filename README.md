# Deltaforge

[![Build status](https://travis-ci.org/Brutus5000/deltaforge.svg?branch=master)](https://travis-ci.org/Brutus5000/deltaforge) 

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


## Sample use case: Forged Alliance

Forged Alliance is a proprietary real-time strategy game. A lot of its game logic is programmed in Lua and can be modified as such. Due to the sheer amount of code a whole rewrite of the Lua code is not possible. Therefore the community is modifying original files as needed.

Due to copyright constraints, the distribution of these modified files is a gray area, as they still contain copyrighted content. The only way to circumvent this problem is to only provide the delta between the copyrighted original file and the modified file.

However, there is not one single source of truth for the original files, as Forged Alliance exists in different patch levels and versions (original retail, gold edition, Steam edition...).
As such, Deltaforge will need to scan the installation of the user and detect which version is used.
From this "source" version, Deltaforge will create an "initial baseline" (this is the only patching activity that is not supposed to be reversible - as it is not necessary). This is one version state that all existing retail editions can reach. 

The "initial baseline" is now the "mothership" of versions that all other patches directly or indirectly channel off.

Deltaforge was specifically designed to solve this use case.
But from the authors experience patching and modding of old games is a common use case and as such, the approach was generified in this project, so it can fit all kind of problems.


## Main concept

Deltaforge organizes everything in repositories. One repository contains one set of assets or files that are supposed to be managed under version control.
The server and client support the managing of multiple repositories.

A repository is organized in a directed graph. Example:

``` 
Source Version A                     v0.9 - v0.8    (channel to reproduce old versions in inverse order)
                 \                 /
Source Version B ‒ Initial Baseline - Changeset A - Changeset B - Changeset C - Changeset n+1    (unstable channel)
                 /                          \                  \          \
Source Version C                             v1.0   ------    v1.1 ----  v2.0     (testing channel)                         
                                              \
                                                v1.0.1 - v1.0.2    (stable channel)
```

In our Forged Alliance use case the different source versions would represent different retail versions.

* Each node is a *tag* and represents a specific set of files.
* Each edge is a *patch*.
  * Whether the edge also contains a reverse patch depends on the *patching strategy*.
  * In general it is always possible to reach any tag from any other tag, except for the source versions.
  * The *patching strategy* tries to reduce the costs of patching. Influencing factors are filesize of the patches and the amount patches itself (implying a required time of IO and CPU processing time).
* *Channels* represent different versioning concepts (e.g. unstable / testing / stable) and has two uses:
  * for the server to know what patches need to be generated for a new tag
  * for the client to always know the latest tag of his chosen channel





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