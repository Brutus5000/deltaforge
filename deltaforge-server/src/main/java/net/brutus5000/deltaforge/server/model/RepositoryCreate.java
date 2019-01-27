package net.brutus5000.deltaforge.server.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

@Data
@Slf4j
public class RepositoryCreate {
    private String name;
    private String initialBaseline;
    @Nullable
    private String gitUrl;
}
