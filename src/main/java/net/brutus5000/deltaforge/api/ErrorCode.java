package net.brutus5000.deltaforge.api;

import lombok.Getter;

@Getter
public enum ErrorCode {
    REPOSITORY_NOT_FOUND(100, "Repository not found", "There is no repository with id  ''{0}''."),
    BRANCH_NOT_FOUND(101, "Branch not found", "There is no branch with id  ''{0}''."),
    TAG_NOT_FOUND(102, "Tag not found", "There is no tag with id  ''{0}''."),
    PATCH_NOT_FOUND(103, "Patch not found", "There is no patch with id  ''{0}''."),
    UNKNOWN_FILE_SOURCE(104, "Creating tag failed", "Unknown fileSource  ''{0}''.");

    private final int code;
    private final String title;
    private final String detail;

    ErrorCode(int code, String title, String detail) {
        this.code = code;
        this.title = title;
        this.detail = detail;
    }

    public String codeAsString() {
        return String.valueOf(code);
    }

}
