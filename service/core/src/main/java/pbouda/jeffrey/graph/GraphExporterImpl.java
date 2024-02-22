package pbouda.jeffrey.graph;

import one.FlameGraph;
import pbouda.jeffrey.repository.model.GraphContent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class GraphExporterImpl implements GraphExporter {

    @Override
    public void export(Path targetPath, GraphContent content) {
        try {
            String flamegraph = getResource("/flamegraph.html");
            String result = flamegraph.replace("$$data$$", escape(content.content().toString()));
            Files.writeString(targetPath, result);
        } catch (IOException e) {
            throw new RuntimeException("Cannot export flamegraph to a file: " + targetPath, e);
        }
    }

    private static String escape(String s) {
        if (s.indexOf('\\') >= 0) s = s.replace("\\", "\\\\");
        if (s.indexOf('\'') >= 0) s = s.replace("'", "\\'");
        return s;
    }

    private static String getResource(String name) {
        try (InputStream stream = FlameGraph.class.getResourceAsStream(name)) {
            if (stream == null) {
                throw new IOException("No resource found");
            }

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[64 * 1024];
            for (int length; (length = stream.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            return result.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Can't load resource with name " + name);
        }
    }
}
