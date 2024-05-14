package pbouda.jeffrey.generator.timeseries;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import jdk.jfr.consumer.RecordedEvent;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.tuple.primitive.LongLongPair;
import org.eclipse.collections.impl.map.mutable.primitive.LongLongHashMap;
import pbouda.jeffrey.common.AbsoluteTimeRange;
import pbouda.jeffrey.common.Type;
import pbouda.jeffrey.jfrparser.jdk.SingleEventProcessor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;

public class TimeseriesEventProcessor extends SingleEventProcessor implements Supplier<ArrayNode> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final LongLongHashMap values = new LongLongHashMap();
    private final long timeShift;
    private final AbsoluteTimeRange timeRange;

    final Function<RecordedEvent, Long> valueExtractor;

    public TimeseriesEventProcessor(Type eventType, Function<RecordedEvent, Long> valueExtractor, AbsoluteTimeRange timeRange) {
        this(eventType, valueExtractor, timeRange, 0);
    }

    public TimeseriesEventProcessor(Type eventType, Function<RecordedEvent, Long> valueExtractor, AbsoluteTimeRange timeRange, long timeShift) {
        super(eventType);
        this.valueExtractor = valueExtractor;
        this.timeShift = timeShift;
        this.timeRange = timeRange;
    }

    @Override
    public Result onEvent(RecordedEvent event) {
        Instant eventTime = event.getStartTime();

        // TimeShift to correlate 2 timeseries and different start-times 
        eventTime = eventTime.plusMillis(timeShift);

        if (eventTime.isBefore(timeRange.start()) || eventTime.isAfter(timeRange.end())) {
            return Result.CONTINUE;
        }

        long second = eventTime.truncatedTo(ChronoUnit.SECONDS)
                .toEpochMilli();

        incrementCounter(event, second);

        return Result.CONTINUE;
    }

    protected void incrementCounter(RecordedEvent event, long second) {
        values.addToValue(second, valueExtractor.apply(event));
    }

    protected ArrayNode buildResult(LongLongHashMap values) {
        ArrayNode result = MAPPER.createArrayNode();
        MutableList<LongLongPair> sorted = values.keyValuesView()
                .toSortedList(Comparator.comparing(LongLongPair::getOne));

        for (LongLongPair pair : sorted) {
            ArrayNode timeSamples = MAPPER.createArrayNode();
            timeSamples.add(pair.getOne());
            timeSamples.add(pair.getTwo());
            result.add(timeSamples);
        }
        return result;
    }

    @Override
    public ArrayNode get() {
        return buildResult(values);
    }
}
