package net.brutus5000.deltaforge.api;

import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.RepositoryService;
import net.brutus5000.deltaforge.model.TagType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@RestController
@Slf4j
public class CreatePatchController {
    private final RepoService repoService;
    private final FileService fileService;
    private final RepositoryService repositoryService;

    public CreatePatchController(RepoService repoService, FileService fileService, RepositoryService repositoryService) {
        this.repoService = repoService;
        this.fileService = fileService;
        this.repositoryService = repositoryService;
    }

    @RequestMapping(path = "api/v1/branches/{branchId}/addTag", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void addTag(HttpServletRequest request, HttpServletResponse response,
                       @PathVariable("branchId") UUID branchId, @RequestParam("tagId") UUID tagId, @RequestParam("tagType") String tagTypeString) {
        repoService.addTagToBranch(branchId, tagId, TagType.parse(tagTypeString));
    }

//    //@ApiOperation("Registers a new account that needs to be activated.")
//    @RequestMapping(path = "", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
//    public Repository addRepository(HttpServletRequest request, HttpServletResponse response,
//                                    @RequestParam("name") String name,
//                                    @RequestParam(name = "gitUrl", required = false) String gitUrl) {
//        return repositoryService.addRepository(name, gitUrl);
//    }
//
//    @RequestMapping(path = "/{repositoryId}/branches", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    public List<Branch> getBranches(HttpServletRequest request, HttpServletResponse response,
//                                    @RequestParam("repositoryId") UUID repositoryId) {
//        // https://stackoverflow.com/questions/20366304/bind-uuid-in-spring-mvc
//        return repositoryService.getBranches(repositoryId);
//    }
//
//    //@ApiOperation("Registers a new account that needs to be activated.")
//    @RequestMapping(path = "/{repositoryId}/branches", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
//    public Branch addBranch(HttpServletRequest request, HttpServletResponse response,
//                            @RequestParam("repositoryId") UUID repositoryId,
//                            @RequestParam("name") String name,
//                            @RequestParam("initialBaselineTagId") UUID initialBaselineTagId,
//                            @RequestParam(name = "gitBranch", required = false) String gitBranch) {
//        return repositoryService.addBranch(repositoryId, name, initialBaselineTagId, gitBranch);
//    }
//
//    @RequestMapping(path = "/{repositoryId}/branches", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    public List<Branch> getTags(HttpServletRequest request, HttpServletResponse response,
//                                @RequestParam("repositoryId") UUID repositoryId) {
//        // https://stackoverflow.com/questions/20366304/bind-uuid-in-spring-mvc
//        return repositoryService.getBranches(repositoryId);
//    }
//
//    //@ApiOperation("Registers a new account that needs to be activated.")
//    @RequestMapping(path = "/{repositoryId}/tags", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
//    public Tag addTag(HttpServletRequest request, HttpServletResponse response,
//                      @RequestParam("repositoryId") UUID repositoryId,
//                      @RequestParam("name") String name,
//                      @RequestParam("fileSource") TagFileSource fileSource,
//                      @RequestParam("fileSourceName") String fileSourceName) {
//
//        switch (fileSource) {
//            case FILE_PATH:
//                return repositoryService.createTagFromFile(repositoryId, name, fileSourceName);
//            case GIT_TAG:
//                return repositoryService.createTagFromGitTag(repositoryId, name, fileSourceName);
//            case GIT_COMMIT:
//                return repositoryService.createTagFromGitCommit(repositoryId, name, fileSourceName);
//            default:
//                throw new ApiException(new Error(ErrorCode.UNKNOWN_FILE_SOURCE, fileSource));
//        }
//    }
}
