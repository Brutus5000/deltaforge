package net.brutus5000.deltaforge.server.repository;

import net.brutus5000.deltaforge.server.model.Branch;
import net.brutus5000.deltaforge.server.model.Repository;
import net.brutus5000.deltaforge.server.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RepositoryRestResource(collectionResourceRel = "branches",
        collectionResourceDescription = @Description("A branch of a repository"))
public interface BranchRepository extends JpaRepository<Branch, UUID> {
    Optional<Branch> findByRepositoryAndName(Repository repository, String name);

    Set<Branch> findAllByCurrentTag(Tag tag);
}
