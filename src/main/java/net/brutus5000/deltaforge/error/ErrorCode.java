package net.brutus5000.deltaforge.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    PROPERTY_IS_NULL(100, "Property is null", "The content of property ''{0}'' must not be null."),
    STRING_IS_EMPTY(101, "String is empty", "Property ''{0}'' must not be empty."),
    VALIDATION_FAILED(102, "Validation failed", "{0}"),
    REPOSITORY_NOT_FOUND(103, "Repository not found", "There is no repository with id  ''{0}''."),
    REPOSITORY_NAME_IN_USE(104, "Repository name in use", "The repository name ''{0}'' is already in use."),
    REPOSITORY_GIT_URL_IN_USE(105, "Repository git url in use", "The repository git url ''{0}'' is already in use."),
    REPOSITORY_BASELINE_FIXED(106, "Repository initialBaseline fixed", "You cannot change the initial baseline after creation."),
    REPOSITORY_FIXED(107, "Repository fixed", "The associated repository can not be changed after creation."),
    BRANCH_NOT_FOUND(108, "Branch not found", "There is no branch with id  ''{0}''."),
    BRANCH_NAME_IN_USE(109, "Branch name in use", "The branch name ''{1}'' is already in use in repository ''{0}''."),
    TAG_NOT_FOUND(110, "Tag not found", "There is no tag with id  ''{0}''."),
    TAG_NAME_IN_USE(111, "Tag name in use", "The tag name ''{1}'' is already in use in repository ''{0}''."),
    TAG_FOLDER_NOT_EXISTS(112, "Tag folder missing", "The folder for the tag does not exists."),
    TAG_INVALID_TYPE(113, "Tag type invalid", "The tag type ''{0}'' is not valid."),
    TAG_ALREADY_ASSIGNED(114, "Tag already assigned", "The tag id ''{0}'' is alrady assigned to branch ''{1}''."),
    PATCH_NOT_FOUND(115, "Patch not found", "There is no patch with id ''{0}''."),
    UNKNOWN_FILE_SOURCE(116, "Creating tag failed", "Unknown fileSource ''{0}''.");

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
