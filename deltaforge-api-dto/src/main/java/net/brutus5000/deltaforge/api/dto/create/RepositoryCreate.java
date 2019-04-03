package net.brutus5000.deltaforge.api.dto.create;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class RepositoryCreate {
    private String name;
    private String initialBaseline;
}
