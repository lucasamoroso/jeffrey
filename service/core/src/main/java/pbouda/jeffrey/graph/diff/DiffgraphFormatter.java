package pbouda.jeffrey.graph.diff;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pbouda.jeffrey.Json;
import pbouda.jeffrey.graph.Frame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DiffgraphFormatter {

    private static final double MIN_SAMPLES_IN_PCT = 0.1;
    private static final double MAX_LEVEL = 1000;

    private static final String[] GREEN_COLORS = {
            "#E5FFCC",
            "#CCFF99",
            "#B2FF66",
            "#99FF33",
            "#66CC00",
    };

    private static final String[] RED_COLORS = {
            "#FFEEEE",
            "#FFCCCC",
            "#FFAAAA",
            "#FF8888",
            "#FF3333",
    };

    private static final String NEUTRAL_COLOR = "#E6E6E6";
    private static final String REMOVED_COLOR = GREEN_COLORS[GREEN_COLORS.length - 1];
    private static final String ADDED_COLOR = RED_COLORS[RED_COLORS.length - 1];

    private final DiffFrame diffFrame;
    private final long minSamples;

    public DiffgraphFormatter(DiffFrame diffFrame) {
        this.diffFrame = diffFrame;

        long totalSamples = diffFrame.baselineSamples + diffFrame.comparisonSamples;
        this.minSamples = (long) (totalSamples * MIN_SAMPLES_IN_PCT / 100);
    }

    public ObjectNode format() {
        List<List<ObjectNode>> output = new ArrayList<>();
        walkLayer(output, diffFrame, 0, 0);

        int depth = output.size();
        ObjectNode data = Json.createObject()
                .put("depth", depth);

        data.set("levels", Json.mapper().valueToTree(output));
        return data;
    }

    private void walkLayer(List<List<ObjectNode>> out, DiffFrame diffFrame, int layer, long x) {
        switch (diffFrame.type) {
            case REMOVED -> removedSubtree(out, diffFrame, layer, x);
            case ADDED -> addedSubtree(out, diffFrame, layer, x);
            case SHARED -> {
                checkAndAddLayer(out, layer);

                ObjectNode jsonFrame = Json.createObject()
                        .put("left", x)
                        .put("total", diffFrame.samples())
                        .put("color", resolveColor(diffFrame))
                        .put("title", StringUtils.escape(diffFrame.methodName));
                jsonFrame.set("details", resolveDetail(diffFrame));

                List<ObjectNode> layerNodes = out.get(layer);
                layerNodes.add(jsonFrame);

                for (Map.Entry<String, DiffFrame> e : diffFrame.entrySet()) {
                    DiffFrame child = e.getValue();
                    if (child.samples() > minSamples) {
                        walkLayer(out, child, layer + 1, x);
                    }
                    x += child.samples();
                }
            }
        }
    }

    private static String resolveColor(DiffFrame diffFrame) {
        float pct = toPercent(diffFrame);

        int index;
        if (pct <= 0.02) {
            return NEUTRAL_COLOR;
        } else if (pct <= 0.1) {
            index = 0;
        } else if (pct <= 0.4) {
            index = 1;
        } else if (pct <= 0.8) {
            index = 2;
        } else {
            index = 3;
        }

        return diffFrame.baselineSamples > diffFrame.comparisonSamples
                ? GREEN_COLORS[index]
                : RED_COLORS[index];
    }

    private static float toPercent(DiffFrame diffFrame) {
        long baselineSamples = diffFrame.baselineSamples;
        long comparisonSamples = diffFrame.comparisonSamples;

        long total = baselineSamples + comparisonSamples;
        long diff = Math.abs(baselineSamples - comparisonSamples);
        float pct = (float) diff / total;
        return (float) Math.round(pct * 100f);
    }

    private static JsonNode resolveDetail(DiffFrame diffFrame) {
        return Json.createObject()
                .put("samples", diffFrame.comparisonSamples - diffFrame.baselineSamples)
                .put("percent", toPercent(diffFrame));
    }

    /**
     * Create layers lazily in cost of bounds checks. Otherwise, we would need to go over two graphs to get the depth.
     *
     * @param out   all nodes in the final graph.
     * @param layer a current requested level.
     */
    private static void checkAndAddLayer(List<List<ObjectNode>> out, int layer) {
        if (out.size() <= layer) {
            out.add(new ArrayList<>());
        }
    }

    private void removedSubtree(List<List<ObjectNode>> out, DiffFrame diffFrame, int layer, long x) {
        oneColorSubtree(out, diffFrame.frame, diffFrame.methodName, layer, x, REMOVED_COLOR, false);
    }

    private void addedSubtree(List<List<ObjectNode>> out, DiffFrame diffFrame, int layer, long x) {
        oneColorSubtree(out, diffFrame.frame, diffFrame.methodName, layer, x, ADDED_COLOR, true);
    }

    private void oneColorSubtree(List<List<ObjectNode>> out, Frame frame, String methodName, int layer, long x, String color, boolean added) {
        checkAndAddLayer(out, layer);

        ObjectNode jsonFrame = Json.createObject()
                .put("left", x)
                .put("total", frame.totalWeight())
                .put("self", frame.selfWeight())
                .put("color", color)
                .put("title", StringUtils.escape(methodName));

        long samples = frame.totalWeight();
        if (!added) {
            samples = ~samples + 1;
        }

        ObjectNode details = Json.createObject()
                .put("samples", samples)
                .put("percent", 100);

        jsonFrame.set("details", details);

        List<ObjectNode> layerNodes = out.get(layer);
        layerNodes.add(jsonFrame);

        for (Map.Entry<String, Frame> e : frame.entrySet()) {
            Frame child = e.getValue();
            String method = e.getKey();
            if (child.totalWeight() > minSamples && MAX_LEVEL > layer) {
                oneColorSubtree(out, child, method, layer + 1, x, color, added);
            }
            x += child.totalWeight();
        }
    }
}
