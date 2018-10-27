package net.brutus5000.deltaforge.api;

import lombok.NonNull;
import net.brutus5000.deltaforge.config.DeltaForgeProperties;
import net.brutus5000.deltaforge.model.Tag;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileService {
    private final DeltaForgeProperties properties;

    public FileService(DeltaForgeProperties properties) {
        this.properties = properties;
    }

    public boolean existsTagFolderPath(@NonNull Tag tag) {
        if (tag.getRepository() == null || tag.getRepository().getName() == null)
            return false;

        if (tag.getName() == null)
            return false;

        return Files.exists(buildTagFolderPath(tag));
    }

    public Path buildTagFolderPath(@NonNull Tag tag) {
        Assert.notNull(tag.getRepository(), "Tag must have a repository");
        Assert.notNull(tag.getName(), "Tag must have a name");
        return Paths.get(properties.getRootRepositoryPath(), tag.getRepository().getName(), tag.getName());
    }
}
