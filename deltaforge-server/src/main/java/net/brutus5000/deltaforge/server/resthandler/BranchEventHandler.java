package net.brutus5000.deltaforge.server.resthandler;

import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.server.error.Error;
import net.brutus5000.deltaforge.server.error.ErrorCode;
import net.brutus5000.deltaforge.server.error.NotFoundApiException;
import net.brutus5000.deltaforge.server.model.Branch;
import net.brutus5000.deltaforge.server.repository.BranchRepository;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

import static net.brutus5000.deltaforge.server.resthandler.ValidationBuilder.whenChanged;


@Component
@Slf4j
@RepositoryEventHandler(Branch.class)
public class BranchEventHandler {
    private final BranchRepository branchRepository;
    private final EntityManager entityManager;

    public BranchEventHandler(BranchRepository branchRepository, EntityManager entityManager) {
        this.branchRepository = branchRepository;
        this.entityManager = entityManager;
    }

    private ValidationBuilder notNullChecks(ValidationBuilder validationBuilder, Branch branch) {
        return validationBuilder
                .assertNotBlank(branch.getName(), "name")
                .assertNotNull(branch.getRepository(), "repository")
                .assertNotNull(branch.getCurrentBaseline(), "currentBaseline")
                .assertNotNull(branch.getCurrentTag(), "currentTag");
    }

    @HandleBeforeCreate
    public void handleBeforeCreate(Branch branch) {
        new ValidationBuilder()
                .apply(branch, this::notNullChecks)
                .assertNotExists(
                        o -> branchRepository.findByRepositoryAndName(o.getRepository(), o.getName()),
                        branch, ErrorCode.BRANCH_NAME_IN_USE, branch.getName())
                .validate();
    }

    @HandleBeforeSave
    public void handleBeforeSave(Branch branch) {
        entityManager.detach(branch);
        Branch preUpdate = branchRepository.findById(branch.getId())
                .orElseThrow(() -> new NotFoundApiException(new Error(ErrorCode.BRANCH_NOT_FOUND, branch.getId())));

        new ValidationBuilder()
                .apply(branch, this::notNullChecks)
                .conditionalAssertNotExists(
                        whenChanged(branch.getName(), preUpdate.getName()),
                        o -> branchRepository.findByRepositoryAndName(o.getRepository(), o.getName()),
                        branch, ErrorCode.BRANCH_NAME_IN_USE, branch.getName())
                .assertUnchanged(branch.getRepository(), preUpdate.getRepository(), ErrorCode.REPOSITORY_FIXED)
                .validate();
    }
}