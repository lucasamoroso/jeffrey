package pbouda.jeffrey.jfr.info;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jdk.jfr.EventType;
import pbouda.jeffrey.common.Json;
import pbouda.jeffrey.common.Type;

import java.util.Map;

public class ExecutionSamplesExtraInfo implements ExtraInfoEnhancer {

    private final Map<String, String> settings;

    public ExecutionSamplesExtraInfo(Map<String, String> settings) {
        this.settings = settings;
    }

    @Override
    public boolean isApplicable(EventType eventType) {
        return Type.EXECUTION_SAMPLE.sameAs(eventType);
    }

    @Override
    public void accept(ObjectNode json) {
        ObjectNode extras = Json.createObject()
                .put("source", settings.get("source"))
                .put("cpu_event", settings.get("cpu_event"));

        json.set("extras", extras);
    }
}
