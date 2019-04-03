package net.brutus5000.deltaforge.server.model.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.brutus5000.deltaforge.server.model.Patch;
import net.brutus5000.deltaforge.server.model.PatchGraph;
import net.brutus5000.deltaforge.server.model.Tag;
import org.apache.commons.io.output.StringBuilderWriter;
import org.jgrapht.Graph;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphMLExporter;

import java.io.IOException;

public class GraphSerializer extends StdSerializer<PatchGraph> {

    private GraphMLExporter<Tag, Patch> exporter
            = new GraphMLExporter<>(Tag::getName, Tag::getName, Patch::getName, Patch::getName);

    public GraphSerializer() {
        this(null);
    }

    protected GraphSerializer(Class<PatchGraph> t) {
        super(t);
    }

    private String exportGraph(Graph<Tag, Patch> graph) throws ExportException {
        StringBuilderWriter writer = new StringBuilderWriter();
        exporter.exportGraph(graph, writer);
        return writer.toString();
    }

    @Override
    public void serialize(PatchGraph value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
        }

        try {
            gen.writeString(exportGraph(value));
        } catch (ExportException e) {
            throw new IOException("Error on exporting GraphML", e);
        }
    }
}
