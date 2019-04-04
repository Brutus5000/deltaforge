package net.brutus5000.deltaforge.api.dto.create;

import lombok.Data;

@Data
public class RepositoryCreate {
    private String name;
    private String initialBaseline;
}
