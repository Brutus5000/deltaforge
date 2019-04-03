package net.brutus5000.deltaforge.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.brutus5000.deltaforge.server.error.ErrorCode;
import net.brutus5000.deltaforge.server.model.Branch;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BranchRepositoryIT {
    @Autowired
    private WebApplicationContext wac;

    @Autowired
    ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    void AsUnauthenticatedUser__GivenNoExistingBranch__WhenGetRequestOnBranches__shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/branchDtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements", is(0)));
    }

    @Test
    void AsUnauthenticatedUser__GivenNoExistingBranch__WhenPostingEmptyBranchObject__shouldFailWithErrors() throws Exception {
        Branch branch = new Branch();

        mockMvc.perform(post("/api/v1/branchDtos")
                .content(objectMapper.writeValueAsString(branch)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errors[*].code", Matchers.contains(
                        ErrorCode.PROPERTY_IS_NULL.codeAsString(),
                        ErrorCode.PROPERTY_IS_NULL.codeAsString(),
                        ErrorCode.PROPERTY_IS_NULL.codeAsString(),
                        ErrorCode.PROPERTY_IS_NULL.codeAsString()
                )))
                .andExpect(jsonPath("$.errors[*].meta.args[*]", containsInAnyOrder(
                        "repository",
                        "name",
                        "currentBaseline",
                        "currentTag"
                )));
    }
}
