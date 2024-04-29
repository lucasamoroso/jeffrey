package pbouda.jeffrey.manager;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pbouda.jeffrey.TimeRangeRequest;
import pbouda.jeffrey.TimeUtils;
import pbouda.jeffrey.WorkingDirs;
import pbouda.jeffrey.common.Config;
import pbouda.jeffrey.common.EventType;
import pbouda.jeffrey.common.TimeRange;
import pbouda.jeffrey.generator.flamegraph.GraphExporter;
import pbouda.jeffrey.generator.flamegraph.flame.FlamegraphGenerator;
import pbouda.jeffrey.generator.timeseries.api.TimeseriesGenerator;
import pbouda.jeffrey.repository.model.GraphContent;
import pbouda.jeffrey.repository.model.GraphInfo;
import pbouda.jeffrey.repository.GraphRepository;
import pbouda.jeffrey.repository.model.ProfileInfo;

import java.nio.file.Path;
import java.util.Optional;

public class DbBasedFlamegraphManager extends AbstractDbBasedGraphManager {

    private final ProfileInfo profileInfo;
    private final FlamegraphGenerator generator;
    private final TimeseriesGenerator timeseriesGenerator;
    private final Path profileRecording;

    public DbBasedFlamegraphManager(
            ProfileInfo profileInfo,
            WorkingDirs workingDirs,
            GraphRepository repository,
            FlamegraphGenerator generator,
            GraphExporter graphExporter,
            TimeseriesGenerator timeseriesGenerator) {

        super(GraphType.DIFFGRAPH, profileInfo, workingDirs, repository, graphExporter);

        this.profileRecording = workingDirs.profileRecording(profileInfo);
        this.profileInfo = profileInfo;
        this.generator = generator;
        this.timeseriesGenerator = timeseriesGenerator;
    }

    @Override
    public Optional<GraphContent> generateComplete(EventType eventType) {
        GraphInfo graphInfo = GraphInfo.complete(profileInfo.id(), eventType);
        Config config = Config.primaryBuilder()
                .withPrimaryRecording(profileRecording)
                .withEventType(eventType)
                .build();

        return generate(true, graphInfo, () -> generator.generate(config));
    }

    @Override
    public ObjectNode generate(EventType eventType) {
        Config config = Config.primaryBuilder()
                .withPrimaryRecording(profileRecording)
                .withEventType(eventType)
                .build();

        return generator.generate(config);
    }

    @Override
    public ObjectNode generate(EventType eventType, TimeRangeRequest timeRange) {
        Config config = Config.primaryBuilder()
                .withPrimaryRecording(profileRecording)
                .withPrimaryStart(profileInfo.startedAt())
                .withEventType(eventType)
                .withTimeRange(TimeRange.create(timeRange.start(), timeRange.end(), timeRange.absoluteTime()))
                .build();

        return generator.generate(config);
    }

    @Override
    public void save(EventType eventType, TimeRangeRequest timeRange, String flamegraphName) {
        GraphInfo graphInfo = GraphInfo.custom(profileInfo.id(), eventType, flamegraphName);
        Config config = Config.primaryBuilder()
                .withPrimaryRecording(profileRecording)
                .withPrimaryStart(profileInfo.startedAt())
                .withEventType(eventType)
                .withTimeRange(TimeRange.create(timeRange.start(), timeRange.end(), timeRange.absoluteTime()))
                .build();

        generateAndSave(graphInfo, () -> generator.generate(config));
    }

    @Override
    public ArrayNode timeseries(EventType eventType) {
        Config config = Config.primaryBuilder()
                .withPrimaryRecording(profileRecording)
                .withEventType(eventType)
                .withPrimaryStart(profileInfo.startedAt())
                .build();

        return timeseriesGenerator.generate(config);
    }

    @Override
    public ArrayNode timeseries(EventType eventType, String searchPattern) {
        Config config = Config.primaryBuilder()
                .withPrimaryRecording(profileRecording)
                .withPrimaryStart(profileInfo.startedAt())
                .withEventType(eventType)
                .withSearchPattern(searchPattern)
                .build();

        return timeseriesGenerator.generate(config);
    }

    @Override
    public String generateFilename(EventType eventType) {
        return profileInfo.id() + "-" + eventType.code() + "-" + TimeUtils.currentDateTime();
    }
}
