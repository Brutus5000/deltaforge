package net.brutus5000.deltaforge.validator;

import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.error.Error;
import net.brutus5000.deltaforge.error.ErrorCode;
import net.brutus5000.deltaforge.error.NotFoundApiException;
import net.brutus5000.deltaforge.model.Tag;
import net.brutus5000.deltaforge.repository.TagRepository;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

import static net.brutus5000.deltaforge.validator.ValidationBuilder.whenChanged;


@Component
@Slf4j
@RepositoryEventHandler(Tag.class)
public class TagEventHandler {
    private final TagRepository tagRepository;
    private final EntityManager entityManager;

    public TagEventHandler(TagRepository TagRepository, EntityManager entityManager) {
        this.tagRepository = TagRepository;
        this.entityManager = entityManager;
    }

    private ValidationBuilder notNullChecks(ValidationBuilder validationBuilder, Tag tag) {
        return validationBuilder
                .assertNotBlank(tag.getName(), "name")
                .assertNotNull(tag.getRepository(), "repository")
                .assertNotNull(tag.getType(), "type");
    }

    @HandleBeforeCreate
    public void handleBeforeCreate(Tag tag) {
        new ValidationBuilder()
                .apply(tag, this::notNullChecks)
                .assertNotExists(o -> tagRepository.findByRepositoryAndName(o.getRepository(), o.getName()),
                        tag, ErrorCode.TAG_NAME_IN_USE, tag.getRepositoryId(), tag.getName())
                .validate();
    }

    @HandleBeforeSave
    public void handleBeforeSave(Tag tag) {
        entityManager.detach(tag);

        Tag preUpdate = tagRepository.findById(tag.getId())
                .orElseThrow(() -> new NotFoundApiException(new Error(ErrorCode.TAG_NOT_FOUND, tag.getId())));

        new ValidationBuilder()
                .apply(tag, this::notNullChecks)
                .assertUnchanged(tag.getRepository(), preUpdate.getRepository(), ErrorCode.REPOSITORY_FIXED)
                .conditionalAssertNotExists(
                        whenChanged(tag.getName(), preUpdate.getName()),
                        o -> tagRepository.findByRepositoryAndName(o.getRepository(), o.getName()),
                        tag, ErrorCode.TAG_NAME_IN_USE, tag.getRepositoryId(), tag.getName()
                )
                .validate();
    }
}