package net.brutus5000.deltaforge.client.patching;

import net.brutus5000.deltaforge.client.model.Patch;
import net.brutus5000.deltaforge.client.model.Repository;
import net.brutus5000.deltaforge.client.model.Tag;
import org.apache.commons.io.output.StringBuilderWriter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphMLExporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class PatchGraphTest {
    private Repository repository;

    private PatchGraph underTest;


    private Graph<Tag, Patch> graph;

    @BeforeEach
    void beforeEach() {
        repository = new Repository()
                .setName("someRepository");

        graph = new DirectedMultigraph<>(Patch.class);

        underTest = new PatchGraph(repository);
    }

    @Test
    void testOneVertexGraph() throws Exception {
        Tag tag = new Tag()
                .setName("someTag");

        addTag(tag);

        repository.setGraph(exportGraph(graph));
        List<Patch> result = underTest.getPatchPath(tag, tag);

        assertThat(result.size(), is(0));
    }

    @Test
    void testTwoVertexOnePatchGraph() throws Exception {
        Tag tagOne = new Tag()
                .setName("Tag1");

        Tag tagTwo = new Tag()
                .setName("Tag2");

        Patch patch = new Patch()
                .setFrom(tagOne)
                .setTo(tagTwo);

        addTag(tagOne);
        addTag(tagTwo);
        addPatch(patch);

        repository.setGraph(exportGraph(graph));

        List<Patch> result = underTest.getPatchPath(tagOne, tagTwo);
        assertThat(result.size(), is(1));

        result = underTest.getPatchPath(tagTwo, tagOne);
        assertThat(result, nullValue());
    }

    private void addTag(Tag tag) {
        repository.getTags().add(tag);
        graph.addVertex(tag);
    }

    private void addPatch(Patch patch) {
        repository.getPatches().add(patch);
        graph.addEdge(patch.getFrom(), patch.getTo(), patch);
    }

    private String exportGraph(Graph<Tag, Patch> graph) throws ExportException {
        StringBuilderWriter writer = new StringBuilderWriter();
        createExporter().exportGraph(graph, writer);
        return writer.toString();
    }

    private GraphMLExporter<Tag, Patch> createExporter() {
        return new GraphMLExporter<>(Tag::getName, Tag::getName, Patch::getName, Patch::getName);
    }
}
