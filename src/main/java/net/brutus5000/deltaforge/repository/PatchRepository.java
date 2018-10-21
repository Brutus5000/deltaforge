package net.brutus5000.deltaforge.repository;

import net.brutus5000.deltaforge.model.Patch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.UUID;

@RepositoryRestResource(collectionResourceRel = "patches",
        collectionResourceDescription = @Description("A patch from one tag to another tag"))
public interface PatchRepository extends JpaRepository<Patch, UUID> {
    @Override
    @RestResource(exported = false)
    <S extends Patch> S save(S entity);

    @Override
    @RestResource(exported = false)
    void deleteById(UUID uuid);

    @Override
    @RestResource(exported = false)
    void delete(Patch entity);
}
