package net.brutus5000.deltaforge.server.api;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.patching.PatchTaskV1;
import net.brutus5000.deltaforge.patching.io.Bsdiff4Service;
import net.brutus5000.deltaforge.patching.io.IoService;
import net.brutus5000.deltaforge.patching.meta.patch.PatchMetadata;
import net.brutus5000.deltaforge.server.config.DeltaforgeServerProperties;
import net.brutus5000.deltaforge.server.events.PatchCreatedEvent;
import net.brutus5000.deltaforge.server.model.Patch;
import net.brutus5000.deltaforge.server.model.PatchTask;
import net.brutus5000.deltaforge.server.model.TaskStatus;
import net.brutus5000.deltaforge.server.repository.PatchRepository;
import net.brutus5000.deltaforge.server.repository.PatchTaskRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
@Slf4j
public class PatchWorker {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DeltaforgeServerProperties properties;
    private final FileService fileService;
    private final PatchRepository patchRepository;
    private final PatchTaskRepository patchTaskRepository;
    private final Bsdiff4Service bsdiff4Service;
    private final IoService ioService;
    private PatchTask current;

    public PatchWorker(ApplicationEventPublisher applicationEventPublisher, DeltaforgeServerProperties properties,
                       FileService fileService, PatchRepository patchRepository, PatchTaskRepository patchTaskRepository,
                       Bsdiff4Service bsdiff4Service, IoService ioService) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.properties = properties;
        this.fileService = fileService;
        this.patchRepository = patchRepository;
        this.patchTaskRepository = patchTaskRepository;
        this.bsdiff4Service = bsdiff4Service;
        this.ioService = ioService;
    }

    @Scheduled(fixedDelay = 1000)
    public void checkQueue() {
        if (current != null) {
            log.debug("Worker still processing existing PatchTask: {}", current);
            return;
        }

        Optional<PatchTask> patchTaskOptional = patchTaskRepository.findFirstByStatusOrderByCreatedAt(TaskStatus.PENDING);

        if (patchTaskOptional.isPresent()) {
            PatchTask patchTask = patchTaskOptional.get();

            log.debug("Next PatchTask: {}", patchTask);

            current = patchTask;

            patchTask.setStatus(TaskStatus.IN_PROCESSING);
            patchTaskRepository.saveAndFlush(patchTask);

            try {
                process(current);
            } finally {
                current = null;
            }
        } else {
            log.trace("No PatchTasks pending");
        }
    }

    public void process(PatchTask patchTask) {
        log.info("Processing PatchTask: {}", patchTask);

        Patch patch = new Patch()
                .setRepository(patchTask.getFrom().getRepository())
                .setFrom(patchTask.getFrom())
                .setTo(patchTask.getTo())
                .setFileSize(800L);

        Path patchDirectory = null;

        try {
            patchDirectory = Files.createTempDirectory("deltaforge_");
            PatchTaskV1 compareTask = new PatchTaskV1(bsdiff4Service, ioService, fileService.buildTagPath(patch.getFrom()),
                    fileService.buildBaselineTagPath(patch.getRepository()), fileService.buildTagPath(patch.getTo()),
                    patchDirectory, patchTask.getTo().getName());
            PatchMetadata metadata = compareTask.compare(patchTask.getFrom().getRepository().getName(), patchTask.getFrom().getName());

            fileService.writeMetadata(patch, metadata);
            fileService.zipPatchFolderContent(patch, patchDirectory);
        } catch (IOException e) {
            log.warn("Processing PatchTask failed: {]", patchTask, e);
        } finally {
            if (patchDirectory != null) {
                try {
                    FileSystemUtils.deleteRecursively(patchDirectory);
                } catch (IOException ignored) {
                }
            }
        }

        log.info("Processing patch task finished: {}", patchTask);
        patchRepository.saveAndFlush(patch);
        applicationEventPublisher.publishEvent(new PatchCreatedEvent(patch, patchTask.getBaselineCheck()));
    }

    @Value
    class Result {
        boolean success;
        long fileSize;
    }
}
