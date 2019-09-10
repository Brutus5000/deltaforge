package net.brutus5000.deltaforge.zipmerger;

import net.brutus5000.deltaforge.patching.io.IoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ZipMergerTest {
    ZipMerger instance;

    @BeforeEach
    void beforeEach() {
        IoService ioService = new IoService();
        instance = new ZipMerger(ioService);
    }

    @Test
    public void testMerge() throws Exception {
        Path scdFilePath = Paths.get("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Supreme Commander Forged Alliance\\gamedata");
        Path gitPath = Paths.get("D:\\dev\\faf\\fa");


        List<Path> gitFolders = Files.list(gitPath)
                .filter(Files::isDirectory)
                .filter(path -> !Objects.equals(path.getFileName().toString(), ".git"))
                .collect(Collectors.toList());

        for (Path folder : gitFolders) {
            instance.merge(folder, scdFilePath.resolve(folder.getFileName() + ".scd"), "ZIP");
        }
    }
}
