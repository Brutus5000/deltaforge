package net.brutus5000.deltaforge.server.resthandler;

import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.server.error.Error;
import net.brutus5000.deltaforge.server.error.ErrorCode;
import net.brutus5000.deltaforge.server.error.NotFoundApiException;
import net.brutus5000.deltaforge.server.model.Repository;
import net.brutus5000.deltaforge.server.repository.RepositoryRepository;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

import static net.brutus5000.deltaforge.server.resthandler.ValidationBuilder.whenChanged;


@Component
@Slf4j
@org.springframework.data.rest.core.annotation.RepositoryEventHandler(Repository.class)
public class RepositoryEventHandler {
    private final RepositoryRepository repositoryRepository;
    private final EntityManager entityManager;

    public RepositoryEventHandler(RepositoryRepository repositoryRepository, EntityManager entityManager) {
        this.repositoryRepository = repositoryRepository;
        this.entityManager = entityManager;
    }

    @HandleBeforeSave
    public void handleBeforeSave(Repository repository) {
        entityManager.detach(repository);

        Repository preUpdate = repositoryRepository.findById(repository.getId())
                .orElseThrow(() -> new NotFoundApiException(new Error(ErrorCode.REPOSITORY_NOT_FOUND, repository.getId())));

        new ValidationBuilder()
                .assertNotBlank(repository.getName(), "name")
                .conditionalAssertNotExists(
                        whenChanged(repository.getName(), preUpdate.getName()),
                        repositoryRepository::findByName, repository.getName(),
                        ErrorCode.REPOSITORY_NAME_IN_USE, repository.getName())
                .assertUnchanged(repository.getInitialBaseline(), preUpdate.getInitialBaseline(), ErrorCode.REPOSITORY_BASELINE_FIXED)
                .validate();
    }
}