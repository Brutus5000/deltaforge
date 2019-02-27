package net.brutus5000.deltaforge.client;

import net.brutus5000.deltaforge.patching.PatchTaskV1;
import net.brutus5000.deltaforge.patching.io.Bsdiff4Service;
import net.brutus5000.deltaforge.patching.io.IoService;
import net.brutus5000.deltaforge.patching.meta.patch.PatchRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PatchService {
    private final Bsdiff4Service bsdiff4Service;
    private final IoService ioService;

    public PatchService(Bsdiff4Service bsdiff4Service, IoService ioService) {
        this.bsdiff4Service = bsdiff4Service;
        this.ioService = ioService;
    }

    public void applyPatch(PatchRequest patchRequest) throws IOException {
        PatchTaskV1 patchTaskV1 = new PatchTaskV1(bsdiff4Service, ioService, patchRequest.getSourceFolder(),
                patchRequest.getInitialBaselineFolder(), patchRequest.getTargetFolder(), patchRequest.getPatchFolder(),
                patchRequest.getRepository());
        patchTaskV1.apply(patchRequest);
    }
}
