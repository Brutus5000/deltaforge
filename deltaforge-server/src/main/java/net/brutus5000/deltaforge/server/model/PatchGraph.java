package net.brutus5000.deltaforge.server.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.brutus5000.deltaforge.server.model.serializer.GraphSerializer;
import org.jgrapht.graph.DirectedWeightedPseudograph;

@JsonSerialize(using = GraphSerializer.class)
public class PatchGraph extends DirectedWeightedPseudograph<Tag, Patch> {
    public PatchGraph() {
        super(Patch.class);
    }

    public void addEdge(Patch patch) {
        this.addEdge(patch.getFrom(), patch.getTo(), patch);
        this.setEdgeWeight(patch, patch.getFileSize());
    }
}
