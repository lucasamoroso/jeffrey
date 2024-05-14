package pbouda.jeffrey.jfr.info;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jdk.jfr.EventType;
import pbouda.jeffrey.common.Json;
import pbouda.jeffrey.common.Type;
import pbouda.jeffrey.jfr.EventSource;

import java.util.Map;

public class AllocationSamplesExtraInfo implements ExtraInfoEnhancer {

    private final Map<String, String> settings;

    public AllocationSamplesExtraInfo(Map<String, String> settings) {
        this.settings = settings;
    }

    @Override
    public boolean isApplicable(EventType eventType) {
        return Type.OBJECT_ALLOCATION_IN_NEW_TLAB.sameAs(eventType);
    }

    @Override
    public void accept(ObjectNode json) {
        if (recordedByAsyncProfiler(settings) && settings.containsKey("alloc_event")) {
            ObjectNode extras = Json.createObject()
                    .put("source", settings.get("source"));

            json.set("extras", extras);
        }
    }

    private static boolean recordedByAsyncProfiler(Map<String, String> settings) {
        return EventSource.ASYNC_PROFILER.name().equals(settings.get("source"));
    }
}
