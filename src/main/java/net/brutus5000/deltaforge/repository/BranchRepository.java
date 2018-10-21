package net.brutus5000.deltaforge.repository;

import net.brutus5000.deltaforge.model.Branch;
import net.brutus5000.deltaforge.model.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(collectionResourceRel = "branches",
        collectionResourceDescription = @Description("A branch of a repository"))
public interface BranchRepository extends JpaRepository<Branch, UUID> {
    Optional<Branch> findByRepositoryAndName(Repository repository, String name);
}