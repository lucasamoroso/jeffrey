package pbouda.jeffrey.manager;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pbouda.jeffrey.TimeRangeRequest;
import pbouda.jeffrey.common.EventType;
import pbouda.jeffrey.repository.model.GraphContent;
import pbouda.jeffrey.repository.model.GraphInfo;
import pbouda.jeffrey.repository.model.ProfileInfo;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface GraphManager {

    @FunctionalInterface
    interface FlamegraphFactory extends Function<ProfileInfo, GraphManager> {
    }

    @FunctionalInterface
    interface DiffgraphFactory extends BiFunction<ProfileInfo, ProfileInfo, GraphManager> {
    }

    List<GraphInfo> allCustom();

    ObjectNode generate(EventType eventType, boolean threadMode);

    ObjectNode generate(EventType eventType, TimeRangeRequest timeRange, boolean threadMode);

    void save(EventType eventType, TimeRangeRequest timeRange, String flamegraphName);

    ArrayNode timeseries(EventType eventType);

    ArrayNode timeseries(EventType eventType, String searchPattern);

    Optional<GraphContent> get(String flamegraphId);

    void export(String flamegraphId);

    void export(EventType eventType, boolean threadMode);

    void export(EventType eventType, TimeRangeRequest timeRange, boolean threadMode);

    void delete(String flamegraphId);

    String generateFilename(EventType eventType);
}
