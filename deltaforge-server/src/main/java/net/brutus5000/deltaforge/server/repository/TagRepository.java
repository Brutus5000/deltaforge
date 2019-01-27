package net.brutus5000.deltaforge.server.repository;

import net.brutus5000.deltaforge.server.model.Repository;
import net.brutus5000.deltaforge.server.model.Tag;
import net.brutus5000.deltaforge.server.model.TagType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RepositoryRestResource(collectionResourceRel = "tags",
        collectionResourceDescription = @Description("A tag pointing to a specific set of binaries"))
public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByRepositoryAndName(Repository repository, String name);

    Set<Tag> findAllByRepository(Repository repository);

    Set<Tag> findAllByRepositoryAndType(Repository repository, TagType type);
}
