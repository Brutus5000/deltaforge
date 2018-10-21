package net.brutus5000.deltaforge;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.brutus5000.deltaforge.error.ErrorCode;
import net.brutus5000.deltaforge.model.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
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
        Repository repository = new Repository();

        mockMvc.perform(post("/api/v1/repositories")
                .content(objectMapper.writeValueAsString(repository)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[*].code", contains(
                        ErrorCode.PROPERTY_IS_NULL.codeAsString()
                )))
                .andExpect(jsonPath("$.errors[*].meta.args[*]", contains(
                        "name"
                )));
    }

    @Test
    void GivenEmptyDatabase__WhenPostRepositoryWithEmptyName__ShouldFailWithErrors() throws Exception {
        Repository repository = new Repository()
                .setName("");

        mockMvc.perform(post("/api/v1/repositories")
                .content(objectMapper.writeValueAsString(repository)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[*].code", contains(
                        ErrorCode.STRING_IS_EMPTY.codeAsString()
                )))
                .andExpect(jsonPath("$.errors[*].meta.args[*]", contains(
                        "name"
                )));
    }

    @Test
    void GivenEmptyDatabase__WhenPostValidRepository__ShouldReturnCreatedRepository() throws Exception {
        Repository repository = new Repository()
                .setName("testRepository");

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/repositories")
                .content(objectMapper.writeValueAsString(repository)))
                .andExpect(status().isCreated())
                .andReturn();

        Repository returned = fetchFromMockMvc(mvcResult);

        repository.setId(repository.getId());

        assertThat(repository.getName(), is(returned.getName()));
    }

    @Test
    void GivenExistingRepositories__WhenPostRepositoryWithExistingName__ShouldFailWithErrors() throws Exception {
        GivenEmptyDatabase__WhenPostValidRepository__ShouldReturnCreatedRepository();

        Repository repository = new Repository()
                .setName("");

        mockMvc.perform(post("/api/v1/repositories")
                .content(objectMapper.writeValueAsString(repository)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[*].code", contains(
                        ErrorCode.STRING_IS_EMPTY.codeAsString()
                )))
                .andExpect(jsonPath("$.errors[*].meta.args[*]", contains(
                        "name"
                )));
    }
}