package net.brutus5000.deltaforge.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.brutus5000.deltaforge.api.dto.create.RepositoryCreate;
import net.brutus5000.deltaforge.server.error.ErrorCode;
import net.brutus5000.deltaforge.server.model.Repository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class RepositoryRepositoryIT {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    private Repository fetchFromMockMvc(MvcResult result) throws Exception {
        return objectMapper.readValue(
                result.getResponse().getContentAsString(),
                Repository.class
        );
    }

    @Test
    void GivenEmptyDatabase__WhenGettAllRepositories__ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/repositories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements", is(0)));
    }

    @Test
    void GivenEmptyDatabase__WhenPostEmptyRepository__ShouldFailWithErrors() throws Exception {
        RepositoryCreate repository = new RepositoryCreate();

        mockMvc.perform(post("/api/v1/repositories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repository)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[*].code", Matchers.containsInAnyOrder(
                        ErrorCode.PROPERTY_IS_NULL.codeAsString(),
                        ErrorCode.PROPERTY_IS_NULL.codeAsString(),
                        ErrorCode.TAG_FOLDER_NOT_EXISTS.codeAsString()
                )))
                .andExpect(jsonPath("$.errors[*].meta.args[*]", containsInAnyOrder(
                        "name",
                        "initialBaseline"
                )));
    }

    @Test
    void GivenEmptyDatabase__WhenPostRepositoryWithEmptyName__ShouldFailWithErrors() throws Exception {
        RepositoryCreate repository = new RepositoryCreate()
                .setName("");

        mockMvc.perform(post("/api/v1/repositories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repository)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[*].code", contains(
                        ErrorCode.STRING_IS_EMPTY.codeAsString(),
                        ErrorCode.PROPERTY_IS_NULL.codeAsString(),
                        ErrorCode.TAG_FOLDER_NOT_EXISTS.codeAsString()
                )))
                .andExpect(jsonPath("$.errors[*].meta.args[*]", contains(
                        "name",
                        "initialBaseline"
                )));
    }

    @Test
    void GivenEmptyDatabase__WhenPostRepositoryMissingFolder__ShouldFailWithErrors() throws Exception {
        RepositoryCreate repository = new RepositoryCreate()
                .setName("testRep")
                .setInitialBaseline("nonExistingFolder");

        mockMvc.perform(post("/api/v1/repositories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repository)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[*].code", contains(
                        ErrorCode.TAG_FOLDER_NOT_EXISTS.codeAsString()
                )));
    }

    @Test
    void GivenEmptyDatabase__WhenPostValidRepository__ShouldReturnCreatedRepository() throws Exception {
        RepositoryCreate repository = new RepositoryCreate()
                .setName("testRepo")
                .setInitialBaseline("initialBaseline");

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/repositories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repository)))
                .andExpect(status().isCreated())
                .andReturn();

        Repository returned = fetchFromMockMvc(mvcResult);

        assertThat(repository.getName(), is(returned.getName()));
    }

    @Test
    void GivenExistingRepositories__WhenPostRepositoryWithExistingName__ShouldFailWithErrors() throws Exception {
        GivenEmptyDatabase__WhenPostValidRepository__ShouldReturnCreatedRepository();

        RepositoryCreate repository = new RepositoryCreate()
                .setName("testRepo")
                .setInitialBaseline("initialBaseline");

        mockMvc.perform(post("/api/v1/repositories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(repository)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[*].code", contains(
                        ErrorCode.REPOSITORY_NAME_IN_USE.codeAsString()
                )))
                .andExpect(jsonPath("$.errors[*].meta.args[*]", contains(
                        "testRepo"
                )));
    }
}