package one;

/*
 * Copyright 2020 Andrei Pangin
 * Modifications copyright (C) 2024 Petr Bouda
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pbouda.jeffrey.Json;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static one.FrameType.FRAME_KERNEL;
import static one.FrameType.FRAME_NATIVE;

public class FlameGraph {
    private final Arguments args;
    private final Frame root = new Frame(FRAME_NATIVE);
    private int depth;
    private long mintotal;

    private static final String[] COLORS = {
            "#b2e1b2",
            "#50e150",
            "#50cccc",
            "#e15a5a",
            "#c8c83c",
            "#e17d00",
            "#cce880"
    };

    public FlameGraph(Arguments args) {
        this.args = args;
    }

    public FlameGraph(String... args) {
        this(new Arguments(args));
    }

    public void parse() throws IOException {
        parse(new InputStreamReader(new FileInputStream(args.input), StandardCharsets.UTF_8));
    }

    public void parse(Reader in) throws IOException {
        try (BufferedReader br = new BufferedReader(in)) {
            for (String line; (line = br.readLine()) != null; ) {
                int space = line.lastIndexOf(' ');
                if (space <= 0) continue;

                String[] trace = line.substring(0, space).split(";");
                long ticks = Long.parseLong(line.substring(space + 1));
                addSample(trace, ticks);
            }
        }
    }

    public void addSample(String[] trace, long ticks) {
        if (excludeTrace(trace)) {
            return;
        }

        Frame frame = root;
        if (args.reverse) {
            for (int i = trace.length; --i >= args.skip; ) {
                frame = frame.addChild(trace[i], ticks);
            }
        } else {
            for (int i = args.skip; i < trace.length; i++) {
                frame = frame.addChild(trace[i], ticks);
            }
        }
        frame.addLeaf(ticks);

        depth = Math.max(depth, trace.length);
    }

//    public void dump() throws IOException {
//        if (args.output == null) {
//            dump(System.out);
//        } else {
//            try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(args.output), 32768);
//                 PrintStream out = new PrintStream(bos, false, StandardCharsets.UTF_8)) {
//                dump(out);
//            }
//        }
//    }

//    public void dump(PrintStream out) {
//        mintotal = (long) (root.total * args.minwidth / 100);
//        int depth = mintotal > 1 ? root.depth(mintotal) : this.depth + 1;
//
//        String tail = getResource("/flame.html");
//
//        tail = printTill(out, tail, "/*height:*/300");
//        out.print(Math.min(depth * 16, 32767));
//
//        tail = printTill(out, tail, "/*title:*/");
//        out.print(args.title);
//
//        tail = printTill(out, tail, "/*reverse:*/false");
//        out.print(args.reverse);
//
//        tail = printTill(out, tail, "/*depth:*/0");
//        out.print(depth);
//
//        tail = printTill(out, tail, "/*frames:*/");
//
//        printFrame(out, "all", root, 0, 0);
//
//        tail = printTill(out, tail, "/*highlight:*/");
//        out.print(args.highlight != null ? "'" + escape(args.highlight) + "'" : "");
//
//        out.print(tail);
//    }

    public ObjectNode dumpToJson() {
        mintotal = (long) (root.total * args.minwidth / 100);
        int depth = mintotal > 1 ? root.depth(mintotal) : this.depth + 1;

        List<List<ObjectNode>> levels = new ArrayList<>();
        for (int i = 0; i < depth; i++) {
            levels.add(new ArrayList<>());
        }

        printFrameJson(levels, "all", root, 0, 0);

        ObjectNode data = Json.createObject()
                .put("height", Math.min(depth * 21, 32767))
                .put("title", args.title)
                .put("reverse", false)
                .put("depth", depth)
                .put("highlight", "");

        data.set("levels", Json.mapper().valueToTree(levels));
        return data;
    }

    public void dumpFromJson(ObjectNode data, PrintStream out) {
        String tail = getResource("/flame.html");

        tail = printTill(out, tail, "/*height:*/300");
        out.print(data.get("height").asInt());

        tail = printTill(out, tail, "/*title:*/");
        out.print(data.get("title").asText("&nbsp;"));

        tail = printTill(out, tail, "/*reverse:*/false");
        out.print(data.get("reverse").asBoolean());

        tail = printTill(out, tail, "/*depth:*/0");
        out.print(data.get("depth").asInt());

        tail = printTill(out, tail, "/*frames:*/");
        ArrayNode frames = (ArrayNode) data.get("frames");
        frames.forEach(frame -> out.println(frame.asText()));

        tail = printTill(out, tail, "/*highlight:*/");
        out.print(data.get("highlight").asText());

        out.print(tail);
    }

    private String printTill(PrintStream out, String data, String till) {
        int index = data.indexOf(till);
        out.print(data.substring(0, index));
        return data.substring(index + till.length());
    }

//    private void printFrame(PrintStream out, String title, Frame frame, int level, long x) {
//        int type = frame.getType();
//        if (type == FRAME_KERNEL) {
//            title = stripSuffix(title);
//        }
//
//        if ((frame.inlined | frame.c1 | frame.interpreted) != 0 && frame.inlined < frame.total && frame.interpreted < frame.total) {
//            out.println("f(" + level + "," + x + "," + frame.total + "," + type + ",'" + escape(title) + "'," +
//                    frame.inlined + "," + frame.c1 + "," + frame.interpreted + ")");
//        } else {
//            out.println("f(" + level + "," + x + "," + frame.total + "," + type + ",'" + escape(title) + "')");
//        }
//
//        x += frame.self;
//        for (Map.Entry<String, Frame> e : frame.entrySet()) {
//            Frame child = e.getValue();
//            if (child.total >= mintotal) {
//                printFrame(out, e.getKey(), child, level + 1, x);
//            }
//            x += child.total;
//        }
//    }

    public Frame getRoot() {
        return root;
    }

    private void printFrameJson(List<List<ObjectNode>> out, String title, Frame frame, int level, long x) {
        int type = frame.getType();
        if (type == FRAME_KERNEL) {
            title = stripSuffix(title);
        }

        ObjectNode jsonFrame = Json.createObject()
                .put("left", x)
                .put("width", frame.total)
                .put("color", COLORS[type])
                .put("title", escape(title))
                .put("details", generateDetail(frame.inlined, frame.c1, frame.interpreted));

        List<ObjectNode> nodesInLayer = out.get(level);
        nodesInLayer.add(jsonFrame);

        for (Map.Entry<String, Frame> e : frame.entrySet()) {
            Frame child = e.getValue();
            if (child.total >= mintotal) {
                printFrameJson(out, e.getKey(), child, level + 1, x);
            }
            x += child.total;
        }
    }

    private static String generateDetail(long inlined, long c1, long interpreted) {
        StringBuilder output = new StringBuilder();
        if (inlined != 0) {
            output.append(", int=").append(inlined);
        }
        if (c1 != 0) {
            output.append(", c1=").append(c1);
        }
        if (interpreted != 0) {
            output.append(", int=").append(interpreted);
        }
        return output.toString();
    }

    private boolean excludeTrace(String[] trace) {
        Pattern include = args.include;
        Pattern exclude = args.exclude;
        if (include == null && exclude == null) {
            return false;
        }

        for (String frame : trace) {
            if (exclude != null && exclude.matcher(frame).matches()) {
                return true;
            }
            if (include != null && include.matcher(frame).matches()) {
                include = null;
                if (exclude == null) break;
            }
        }

        return include != null;
    }

    static String stripSuffix(String title) {
        return title.substring(0, title.length() - 4);
    }

    static String escape(String s) {
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
