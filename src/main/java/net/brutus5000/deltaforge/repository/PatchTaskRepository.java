package net.brutus5000.deltaforge.repository;

import net.brutus5000.deltaforge.model.PatchTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.Description;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.UUID;

@RepositoryRestResource(collectionResourceRel = "patchTasks",
        collectionResourceDescription = @Description("A queue/journal record of patch creation job"))
public interface PatchTaskRepository extends JpaRepository<PatchTask, UUID> {
    @Override
    @RestResource(exported = false)
    <S extends PatchTask> S save(S entity);

    @Override
    @RestResource(exported = false)
    void deleteById(UUID uuid);

    @Override
    @RestResource(exported = false)
    void delete(PatchTask entity);
}
