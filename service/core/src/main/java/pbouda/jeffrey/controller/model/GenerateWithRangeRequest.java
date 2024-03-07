package pbouda.jeffrey.controller.model;

import pbouda.jeffrey.TimeRange;
import pbouda.jeffrey.common.EventType;

public record GenerateWithRangeRequest(String primaryProfileId, String name, EventType eventType, TimeRange timeRange) {
}
