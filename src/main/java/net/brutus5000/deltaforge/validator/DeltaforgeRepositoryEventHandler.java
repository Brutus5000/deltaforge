package net.brutus5000.deltaforge.validator;

import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.error.Error;
import net.brutus5000.deltaforge.error.ErrorCode;
import net.brutus5000.deltaforge.error.NotFoundApiException;
import net.brutus5000.deltaforge.model.Repository;
import net.brutus5000.deltaforge.repository.RepositoryRepository;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

import static net.brutus5000.deltaforge.validator.ValidationBuilder.*;


@Component
@Slf4j
@RepositoryEventHandler(Repository.class)
public class DeltaforgeRepositoryEventHandler {
    private final RepositoryRepository repositoryRepository;
    private final EntityManager entityManager;

    public DeltaforgeRepositoryEventHandler(RepositoryRepository repositoryRepository, EntityManager entityManager) {
        this.repositoryRepository = repositoryRepository;
        this.entityManager = entityManager;
    }

    @HandleBeforeCreate
    public void handleBeforeCreate(Repository repository) {
        new ValidationBuilder()
                .assertNotBlank(repository.getName(), "name")
                .assertNotExists(
                        repositoryRepository::findByName, repository.getName(),
                        ErrorCode.REPOSITORY_NAME_IN_USE, repository.getName())
                .conditionalAssertNotExists(
                        whenNotNull(repository.getGitUrl()),
                        repositoryRepository::findByGitUrl, repository.getGitUrl(),
                        ErrorCode.REPOSITORY_GIT_URL_IN_USE, repository.getName())
                .validate();
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
                .conditionalAssertNotExists(
                        and(
                                whenChanged(repository.getGitUrl(), preUpdate.getGitUrl()),
                                whenNotNull(repository.getGitUrl())
                        ),
                        repositoryRepository::findByGitUrl, repository.getGitUrl(),
                        ErrorCode.REPOSITORY_GIT_URL_IN_USE, repository.getName())
                .validate();
    }
}