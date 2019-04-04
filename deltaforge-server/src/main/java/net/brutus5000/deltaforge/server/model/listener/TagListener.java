package net.brutus5000.deltaforge.server.model.listener;

import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.patching.io.ValidationService;
import net.brutus5000.deltaforge.patching.meta.validate.ValidateMetadata;
import net.brutus5000.deltaforge.server.api.RepoService;
import net.brutus5000.deltaforge.server.events.TagCreatedEvent;
import net.brutus5000.deltaforge.server.model.Tag;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.persistence.PostPersist;
import javax.persistence.PrePersist;
import java.io.IOException;
import java.nio.file.Path;

@Component
@Slf4j
public class TagListener {
    private static ApplicationEventPublisher applicationEventPublisher;
    private static ValidationService validationService;
    private static RepoService repoService;

    @Inject
    public void init(ApplicationEventPublisher applicationEventPublisher,
                     ValidationService validationService,
                     RepoService repoService) {
        TagListener.applicationEventPublisher = applicationEventPublisher;
        TagListener.validationService = validationService;
        TagListener.repoService = repoService;
    }

    @PrePersist
    public void prePersist(Tag tag) throws IOException {
        log.debug("Adding validateMetadata to tag: {}", tag);
        Path tagPath = repoService.getTagPath(tag);
        ValidateMetadata validateMetadata = validationService.buildValidationMetadata(tag.getRepository().getName(), tag.getName(), tagPath);
        tag.setValidateMetadata(validateMetadata);
    }

    @PostPersist
    public void postPersist(Tag tag) {
        log.debug("Publish TagCreatedEvent for tag: {}", tag);
        applicationEventPublisher.publishEvent(new TagCreatedEvent(tag));
    }
}
