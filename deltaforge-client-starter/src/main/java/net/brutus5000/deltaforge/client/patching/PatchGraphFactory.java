package net.brutus5000.deltaforge.client.patching;

import net.brutus5000.deltaforge.client.model.Repository;
import org.springframework.stereotype.Component;

@Component
public class PatchGraphFactory {
    public PatchGraph buildPatchGraph(Repository repository) {
        return new PatchGraph(repository);
    }
}
