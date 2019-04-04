package net.brutus5000.deltaforge.client;

import net.brutus5000.deltaforge.client.model.Repository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Paths;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DeltaforgeClientTestApplication.class)
@ActiveProfiles("integration")
class DeltaforgeClientITCase {
    @Autowired
    DeltaforgeClient deltaforgeClient;

    @Test
    void contextLoads() {
    }

    @Disabled("Only for manual testing when local repository is not loaded yet")
    @Test
    void testLoadRepository() throws Exception {
        Optional<Repository> repositoryOptional = deltaforgeClient.loadRepository("myRepo");

        assertThat(repositoryOptional.isPresent(), is(false));
    }

    @Disabled("Only for manual testing")
    @Test
    void testAddRepository() throws Exception {
        Repository repository = deltaforgeClient.addRepository("myRepo",
                Paths.get("out", "test-data", "source"));
    }
}
