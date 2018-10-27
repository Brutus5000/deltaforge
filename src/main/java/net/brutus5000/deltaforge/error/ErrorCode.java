package net.brutus5000.deltaforge.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    PROPERTY_IS_NULL(100, "Property is null", "The content of property ''{0}'' must not be null."),
    STRING_IS_EMPTY(100, "String is empty", "Property ''{0}'' must not be empty."),
    VALIDATION_FAILED(100, "Validation failed", "{0}"),
    REPOSITORY_NOT_FOUND(100, "Repository not found", "There is no repository with id  ''{0}''."),
    REPOSITORY_NAME_IN_USE(100, "Repository name in use", "The repository name ''{0}'' is already in use."),
    REPOSITORY_GIT_URL_IN_USE(100, "Repository git url in use", "The repository git url ''{0}'' is already in use."),
    REPOSITORY_FIXED(100, "Repository fixed", "The associated repository can not be changed after creation."),
    BRANCH_NOT_FOUND(101, "Branch not found", "There is no branch with id  ''{0}''."),
    BRANCH_NAME_IN_USE(100, "Branch name in use", "The branch name ''{1}'' is already in use in repository ''{0}''."),
    BRANCH_BASELINE_FIXED(100, "Branch baseline fixed", "You cannot change the initial baseline after creation."),
    TAG_NOT_FOUND(102, "Tag not found", "There is no tag with id  ''{0}''."),
    TAG_NAME_IN_USE(100, "Tag name in use", "The tag name ''{1}'' is already in use in repository ''{0}''."),
    TAG_FOLDER_NOT_EXISTS(100, "Tag folder missing", "The folder for the tag does not exists."),
    PATCH_NOT_FOUND(103, "Patch not found", "There is no patch with id ''{0}''."),
    UNKNOWN_FILE_SOURCE(104, "Creating tag failed", "Unknown fileSource ''{0}''.");

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
