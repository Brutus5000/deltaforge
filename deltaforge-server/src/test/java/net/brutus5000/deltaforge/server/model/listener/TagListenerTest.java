package net.brutus5000.deltaforge.server.model.listener;

import net.brutus5000.deltaforge.patching.io.IoService;
import net.brutus5000.deltaforge.patching.io.ValidationService;
import net.brutus5000.deltaforge.patching.meta.validate.ValidateMetadata;
import net.brutus5000.deltaforge.server.api.RepoService;
import net.brutus5000.deltaforge.server.error.ApiException;
import net.brutus5000.deltaforge.server.error.ErrorCode;
import net.brutus5000.deltaforge.server.events.TagCreatedEvent;
import net.brutus5000.deltaforge.server.model.Repository;
import net.brutus5000.deltaforge.server.model.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.nio.file.Path;
import java.util.UUID;

import static net.brutus5000.deltaforge.server.error.ApiExceptionWithCode.apiExceptionWithCode;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TagListenerTest {
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private ValidationService validationService;

    @Mock
    private RepoService repoService;

    @Mock
    private IoService ioService;

    private TagListener tagListener;

    private Tag tag;

    @BeforeEach
    void beforeEach() {
        tagListener = new TagListener();
        tagListener.init(
                applicationEventPublisher,
                validationService,
                repoService,
                ioService
        );

        tag = new Tag()
                .setId(UUID.randomUUID())
                .setName("someTag");
    }

    @AfterEach
    void afterEach() {
        verifyNoMoreInteractions(applicationEventPublisher, validationService, repoService, ioService);
    }

    @Test
    void prePersistTagFolderMissing() {
        Path tagPath = mock(Path.class);

        doReturn(tagPath).when(repoService).getTagPath(tag);
        doReturn(false).when(ioService).isDirectory(tagPath);

        ApiException result = assertThrows(ApiException.class, () -> tagListener.prePersist(tag));

        assertThat(result, is(apiExceptionWithCode(ErrorCode.TAG_FOLDER_NOT_EXISTS)));

        verify(repoService).getTagPath(tag);
    }

    @Test
    void prePersistSuccess() throws Exception {
        Path tagPath = mock(Path.class);
        ValidateMetadata validateMetadata = mock(ValidateMetadata.class);

        Repository repository = new Repository().setName("someRepo");
        tag.setRepository(repository);

        doReturn(tagPath).when(repoService).getTagPath(tag);
        doReturn(true).when(ioService).isDirectory(tagPath);
        doReturn(validateMetadata).when(validationService).buildValidationMetadata(repository.getName(), tag.getName(), tagPath);

        tagListener.prePersist(tag);

        verify(repoService).getTagPath(tag);
        verify(validationService).buildValidationMetadata(repository.getName(), tag.getName(), tagPath);

        assertThat(tag.getValidateMetadata(), is(validateMetadata));
    }

    @Test
    void postPersist() {
        tagListener.postPersist(tag);

        ArgumentCaptor<TagCreatedEvent> tagCreatedEventArgumentCaptor = ArgumentCaptor.forClass(TagCreatedEvent.class);
        verify(applicationEventPublisher).publishEvent(tagCreatedEventArgumentCaptor.capture());
        TagCreatedEvent event = tagCreatedEventArgumentCaptor.getValue();
        assertThat(event.getTag(), is(tag));
    }
}
