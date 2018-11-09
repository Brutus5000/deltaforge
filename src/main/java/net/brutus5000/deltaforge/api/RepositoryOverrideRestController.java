package net.brutus5000.deltaforge.api;

import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.model.Repository;
import net.brutus5000.deltaforge.model.RepositoryCreate;
import net.brutus5000.deltaforge.resthandler.TagEventHandler;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RepositoryRestController
@Slf4j
@ExposesResourceFor(Repository.class)
public class RepositoryOverrideRestController {
    private final RepoService repoService;
    private final TagEventHandler tagEventHandler;
    private final EntityLinks entityLinks;

    public RepositoryOverrideRestController(RepoService repoService, TagEventHandler tagEventHandler, EntityLinks entityLinks) {
        this.repoService = repoService;
        this.tagEventHandler = tagEventHandler;
        this.entityLinks = entityLinks;
    }

    @RequestMapping(path = "repositories", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody
    Resource<Repository> createRepository(HttpServletRequest request, HttpServletResponse response,
                                          PersistentEntityResourceAssembler assembler,
                                          @RequestBody RepositoryCreate repositoryCreate) {
        Repository repository = repoService.createRepository(repositoryCreate);
        Resource<Repository> repositoryResource = (Resource<Repository>) (Object) assembler.toFullResource(repository);

        return repositoryResource;
    }
}
