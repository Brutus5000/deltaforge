package net.brutus5000.deltaforge.repository;

import net.brutus5000.deltaforge.model.Repository;
import net.brutus5000.deltaforge.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(collectionResourceRel = "tags",
        collectionResourceDescription = @Description("A tag pointing to a specific set of binaries"))
public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByRepositoryAndName(Repository repository, String name);
}
