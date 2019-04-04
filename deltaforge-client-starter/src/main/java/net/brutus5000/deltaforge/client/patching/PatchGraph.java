package net.brutus5000.deltaforge.client.patching;

import net.brutus5000.deltaforge.client.error.CheckoutException;
import net.brutus5000.deltaforge.client.model.Patch;
import net.brutus5000.deltaforge.client.model.Repository;
import net.brutus5000.deltaforge.client.model.Tag;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.io.GraphMLImporter;
import org.jgrapht.io.ImportException;

import java.io.StringReader;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

public class PatchGraph {
    private final Repository repository;

    private Graph<Tag, Patch> graph;

    PatchGraph(Repository repository) {
        this.repository = repository;
    }

    private Graph<Tag, Patch> getGraph() throws CheckoutException {
        if (graph == null) {
            refreshGraph();
        }

        return graph;
    }

    public void refreshGraph() throws CheckoutException {
        GraphMLImporter<Tag, Patch> importer = new GraphMLImporter<>(
                (id, attributes) -> repository.getTags().stream()
                        .filter(tag -> Objects.equals(id, tag.getName()))
                        .findFirst()
                        .orElseThrow(() -> new CheckoutException("Unknown tag with id: " + id)),
                (from, to, label, attributes) -> repository.getPatches().stream()
                        .filter(patch -> Objects.equals(patch.getFrom(), from) &&
                                Objects.equals(patch.getTo(), to))
                        .findFirst()
                        .orElseThrow(() -> new CheckoutException(MessageFormat.format("No patch found from {0} to {1}", from.getName(), to.getName())))
        );

        try {
            graph = new DirectedMultigraph<>(Patch.class);
            importer.importGraph(graph, new StringReader(repository.getPatchGraph()));
        } catch (ImportException e) {
            throw new CheckoutException("Error on reading patch patchGraph", e);
        }
    }

    public List<Patch> getPatchPath(Tag from, Tag to) {
        GraphPath<Tag, Patch> graphPath = DijkstraShortestPath.findPathBetween(getGraph(), from, to);

        if (graphPath == null) {
            return null;
        } else {
            return graphPath.getEdgeList();
        }
    }
}
