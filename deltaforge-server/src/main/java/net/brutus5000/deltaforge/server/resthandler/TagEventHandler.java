package net.brutus5000.deltaforge.server.resthandler;

import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.server.api.FileService;
import net.brutus5000.deltaforge.server.error.Error;
import net.brutus5000.deltaforge.server.error.ErrorCode;
import net.brutus5000.deltaforge.server.error.NotFoundApiException;
import net.brutus5000.deltaforge.server.events.TagCreatedEvent;
import net.brutus5000.deltaforge.server.model.Tag;
import net.brutus5000.deltaforge.server.repository.TagRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;

import static net.brutus5000.deltaforge.server.resthandler.ValidationBuilder.whenChanged;


@Component
@Slf4j
@RepositoryEventHandler(Tag.class)
public class TagEventHandler {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final FileService fileService;
    private final TagRepository tagRepository;
    private final EntityManager entityManager;

    public TagEventHandler(ApplicationEventPublisher applicationEventPublisher, FileService fileService, TagRepository TagRepository, EntityManager entityManager) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.fileService = fileService;
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
                .assertThat(fileService::existsTagFolderPath, tag, ErrorCode.TAG_FOLDER_NOT_EXISTS)
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

    @HandleAfterCreate
    public void handleAfterCreate(Tag tag) {
        applicationEventPublisher.publishEvent(new TagCreatedEvent(tag));
    }
}