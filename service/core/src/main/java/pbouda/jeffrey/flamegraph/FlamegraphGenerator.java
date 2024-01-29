package pbouda.jeffrey.flamegraph;

import pbouda.jeffrey.common.EventType;

import java.nio.file.Path;

public interface FlamegraphGenerator {

    byte[] generate(Path profilePath, EventType type);

    byte[] generate(Path profilePath, EventType type, long startMillis, long endMillis);

}
