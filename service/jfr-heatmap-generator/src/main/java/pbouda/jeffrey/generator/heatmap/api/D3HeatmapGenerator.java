package pbouda.jeffrey.generator.heatmap.api;

import pbouda.jeffrey.generator.heatmap.D3HeatmapEventProcessor;
import pbouda.jeffrey.generator.heatmap.RecordingFileIterator;
import pbouda.jeffrey.generator.heatmap.VMStartTimeProcessor;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;

/**
 * Generate a data-file for the specific D3 heatmap from a selected event from JFR file.
 */
public class D3HeatmapGenerator implements HeatmapGenerator {

    @Override
    public void generate(Path jfrFile, OutputStream output, String eventName) {
        _generate(jfrFile, output, eventName);
    }

    @Override
    public byte[] generate(Path jfrFile, String eventName) {
        var baos = new ByteArrayOutputStream();
        _generate(jfrFile, baos, eventName);
        return baos.toByteArray();
    }

    private static void _generate(Path jfrFile, OutputStream output, String eventName) {
        VMStartTimeProcessor vmStartTimeProcessor = new VMStartTimeProcessor();

        RecordingFileIterator iterator = new RecordingFileIterator(jfrFile);
        iterator.iterate(vmStartTimeProcessor);
        iterator.iterate(new D3HeatmapEventProcessor(eventName, vmStartTimeProcessor.startTime(), output));
    }
}
