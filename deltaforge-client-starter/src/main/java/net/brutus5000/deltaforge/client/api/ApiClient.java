package net.brutus5000.deltaforge.client.api;

import net.brutus5000.deltaforge.client.model.Repository;

import java.io.IOException;

public interface ApiClient {
    Repository getRepository(String repositoryName) throws IOException;
}
