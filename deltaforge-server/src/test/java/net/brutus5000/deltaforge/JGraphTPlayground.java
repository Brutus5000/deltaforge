package net.brutus5000.deltaforge;

import net.brutus5000.deltaforge.model.Patch;
import net.brutus5000.deltaforge.model.Tag;
import org.jgrapht.graph.DirectedWeightedPseudograph;
import org.jgrapht.io.GraphMLExporter;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.UUID;

public class JGraphTPlayground {
    @Test
    public void test() throws Exception {
        DirectedWeightedPseudograph<Tag, Patch> graph = new DirectedWeightedPseudograph<>(Patch.class);

        Tag sourceVertex = new Tag()
                .setId(UUID.randomUUID())
                .setName("source");
        Tag targetVertex = new Tag()
                .setId(UUID.randomUUID())
                .setName("target");
        Patch e = new Patch()
                .setId(UUID.randomUUID())
                .setFrom(sourceVertex)
                .setTo(targetVertex)
                .setFileSize(10L);
        graph.addVertex(sourceVertex);
        graph.addVertex(targetVertex);
        graph.addEdge(sourceVertex, targetVertex, e);
        graph.setEdgeWeight(e, e.getFileSize());

        GraphMLExporter<Tag, Patch> gmlExporter = new GraphMLExporter<>(vertex -> vertex.getId().toString(), Tag::getName,
                edge -> edge.getId().toString(), edge -> String.format("%s -> %s", edge.getFrom().getName(), edge.getTo().getName()));

        StringWriter writer = new StringWriter();
        gmlExporter.exportGraph(graph, writer);
        System.out.println(writer.toString());
    }
}
