package net.brutus5000.deltaforge.api;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.config.DeltaForgeProperties;
import net.brutus5000.deltaforge.events.PatchCreatedEvent;
import net.brutus5000.deltaforge.model.Patch;
import net.brutus5000.deltaforge.model.PatchTask;
import net.brutus5000.deltaforge.model.TaskStatus;
import net.brutus5000.deltaforge.patching.Bsdiff4Service;
import net.brutus5000.deltaforge.repository.PatchRepository;
import net.brutus5000.deltaforge.repository.PatchTaskRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class PatchWorker {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DeltaForgeProperties properties;
    private final PatchRepository patchRepository;
    private final PatchTaskRepository patchTaskRepository;
    private final Bsdiff4Service bsdiff4Service;
    private PatchTask current;

    public PatchWorker(ApplicationEventPublisher applicationEventPublisher, DeltaForgeProperties properties,
                       PatchRepository patchRepository, PatchTaskRepository patchTaskRepository,
                       Bsdiff4Service bsdiff4Service) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.properties = properties;
        this.patchRepository = patchRepository;
        this.patchTaskRepository = patchTaskRepository;
        this.bsdiff4Service = bsdiff4Service;
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

        try {
            Thread.sleep(1000);
            //CompareTaskV1 compareTask = new CompareTaskV1(bsdiff4Service, patchTask.getFrom().getRepository().ge)
        } catch (InterruptedException ignored) {
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
