package net.brutus5000.deltaforge.client.api;

import net.brutus5000.deltaforge.client.model.Repository;

import java.io.IOException;
import java.util.Optional;

/**
 * The client-relevant interface of the server api.
 */
public interface ApiClient {
    Optional<Repository> getRepository(String repositoryName) throws IOException;
}
