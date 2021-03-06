package net.brutus5000.deltaforge.server.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.api.dto.create.RepositoryCreate;
import net.brutus5000.deltaforge.patching.io.Zipper;
import net.brutus5000.deltaforge.patching.meta.patch.PatchMetadata;
import net.brutus5000.deltaforge.server.config.DeltaforgeServerProperties;
import net.brutus5000.deltaforge.server.error.ApiException;
import net.brutus5000.deltaforge.server.error.ErrorCode;
import net.brutus5000.deltaforge.server.model.*;
import net.brutus5000.deltaforge.server.repository.*;
import org.apache.commons.compress.archivers.ArchiveException;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import static org.apache.commons.compress.archivers.ArchiveStreamFactory.ZIP;

@Service
@Slf4j
public class FileService {
    private final static String TAGS_FOLDER = "tags";
    private final static String PATCH_FOLDER = "patches";
    private final static String PATCH_CONNECTOR = "__to__";

    private final ObjectMapper objectMapper;
    private final DeltaforgeServerProperties properties;
    private final RepositoryRepository repositoryRepository;
    private final ChannelRepository channelRepository;
    private final TagRepository tagRepository;
    private final TagAssignmentRepository tagAssignmentRepository;
    private final PatchTaskRepository patchTaskRepository;

    public FileService(ObjectMapper objectMapper, DeltaforgeServerProperties properties, RepositoryRepository repositoryRepository, ChannelRepository channelRepository, TagRepository tagRepository, TagAssignmentRepository tagAssignmentRepository, PatchTaskRepository patchTaskRepository) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.repositoryRepository = repositoryRepository;
        this.channelRepository = channelRepository;
        this.tagRepository = tagRepository;
        this.tagAssignmentRepository = tagAssignmentRepository;
        this.patchTaskRepository = patchTaskRepository;
    }

    public boolean existsTagFolderPath(@NonNull RepositoryCreate repositoryCreate) {
        Path path = buildTagFolderPath(repositoryCreate.getName(), repositoryCreate.getInitialBaseline());
        if (path == null) {
            return false;
        }

        return Files.exists(path);
    }

    public boolean existsTagFolderPath(@NonNull Tag tag) {
        if (tag.getRepository() == null || tag.getRepository().getName() == null)
            return false;

        if (tag.getName() == null)
            return false;

        return Files.exists(buildTagFolderPath(tag.getRepository().getName(), tag.getName()));
    }

    public Path buildTagFolderPath(String repositoryName, String tagName) {
        try {
            Assert.notNull(repositoryName, "repositoryName must not be null");
            Assert.notNull(tagName, "tagName must not be null");
            return Paths.get(properties.getRootRepositoryPath(), repositoryName, TAGS_FOLDER, tagName);
        } catch (Exception ignored) {
            return null;
        }
    }

    public void createChannel(@NonNull UUID repositoryId, @NonNull String name, @NonNull UUID baseTagId) {
        Repository repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> ApiException.of(ErrorCode.REPOSITORY_NOT_FOUND, repositoryId));

        Tag baseTag = tagRepository.findById(baseTagId)
                .orElseThrow(() -> ApiException.of(ErrorCode.TAG_NOT_FOUND, baseTagId));

        if (StringUtils.isEmpty(name)) {
            throw ApiException.of(ErrorCode.STRING_IS_EMPTY, name);
        }

        channelRepository.findByRepositoryAndName(repository, name)
                .ifPresent(channel -> {
                    throw ApiException.of(ErrorCode.CHANNEL_NAME_IN_USE, channel.getName());
                });

        Channel newChannel = new Channel()
                .setRepository(repository)
                .setName(name)
                .setCurrentBaseline(baseTag)
                .setCurrentTag(baseTag);

        log.info("Creating new channel: {}", newChannel);
        channelRepository.save(newChannel);

        TagAssignment tagAssignment = new TagAssignment()
                .setChannel(newChannel)
                .setTag(baseTag);

        log.debug("Creating tag assignment for new channel id '{}': {}", newChannel.getId(), tagAssignment);
        tagAssignmentRepository.save(tagAssignment);
    }

    public void addSourceTagToChannel(@NonNull UUID channelId, @NonNull UUID sourceTagId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> ApiException.of(ErrorCode.CHANNEL_NOT_FOUND, channelId));

        Tag sourceTag = tagRepository.findById(sourceTagId)
                .orElseThrow(() -> ApiException.of(ErrorCode.TAG_NOT_FOUND, sourceTagId));

        TagAssignment tagAssignment = new TagAssignment()
                .setChannel(channel)
                .setTag(sourceTag);

        log.debug("Creating tag assignment for channelDto id '{}': {}", channel.getId(), tagAssignment);
        tagAssignmentRepository.save(tagAssignment);

        PatchTask patchTask = new PatchTask()
                .setStatus(TaskStatus.PENDING)
                .setFrom(sourceTag)
                .setTo(channel.getRepository().getInitialBaseline());

        log.debug("Creating patch task: {}", patchTask);
        patchTaskRepository.save(patchTask);
    }

    public Path buildBaselineTagPath(@NonNull Repository repository) {
        return buildTagFolderPath(repository.getName(), repository.getInitialBaseline().getName());
    }

    public Path buildTagPath(@NonNull Tag tag) {
        return buildTagFolderPath(tag.getRepository().getName(), tag.getName());
    }

    public Path buildPatchPath(@NonNull Patch patch, @NonNull String fileExtension) {
        String repositoryName = patch.getRepository().getName();

        return Paths.get(properties.getRootRepositoryPath(), repositoryName, PATCH_FOLDER,
                patch.getFrom().getName() + PATCH_CONNECTOR + patch.getTo().getName() + "." + fileExtension);
    }

    @SneakyThrows
    public void writeMetadata(@NonNull Patch patch, @NonNull PatchMetadata metadata) {
        Path jsonPath = buildPatchPath(patch, "json");
        Files.createDirectories(jsonPath.getParent());
        Files.writeString(jsonPath, objectMapper.writeValueAsString(metadata), StandardOpenOption.CREATE_NEW);
    }

    public void zipPatchFolderContent(@NonNull Patch patch, @NonNull Path patchDirectory) throws IOException {
        Path patchPath = buildPatchPath(patch, "zip");
        Files.createDirectories(patchPath.getParent());

        try {
            Zipper.contentOf(patchDirectory, ZIP).to(patchPath).zip();
        } catch (ArchiveException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            } else {
                throw new IOException("Archiving failed", e);
            }
        }
    }
}
