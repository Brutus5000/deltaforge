package net.brutus5000.deltaforge.server.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@Slf4j
public class CreatePatchController {
    private final RepoService repoService;

    public CreatePatchController(RepoService repoService) {
        this.repoService = repoService;
    }

    @RequestMapping(path = "action/channels/{channelId}/addTag", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void addTag(@PathVariable("channelId") UUID channelId,
                       @RequestParam("tagId") UUID tagId,
                       @RequestParam("tagType") String tagTypeString) {
        repoService.addTagToChannel(channelId, tagId, tagTypeString);
    }
}
