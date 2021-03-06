package net.brutus5000.deltaforge.server.repository;

import net.brutus5000.deltaforge.server.model.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(collectionResourceRel = "repositories",
        collectionResourceDescription = @Description("A repository under binary version control"))
public interface RepositoryRepository extends JpaRepository<Repository, UUID> {
    Optional<Repository> findByName(String name);

    @Override
    @RestResource(exported = false)
    void deleteById(UUID uuid);

    @Override
    @RestResource(exported = false)
    void delete(Repository entity);
}
