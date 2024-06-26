/*
 * Jeffrey
 * Copyright (C) 2024 Petr Bouda
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pbouda.jeffrey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pbouda.jeffrey.generator.flamegraph.GraphExporterImpl;
import pbouda.jeffrey.generator.flamegraph.diff.DiffgraphGeneratorImpl;
import pbouda.jeffrey.generator.flamegraph.flame.FlamegraphGeneratorImpl;
import pbouda.jeffrey.generator.heatmap.api.HeatmapGeneratorImpl;
import pbouda.jeffrey.generator.timeseries.api.TimeseriesGeneratorImpl;
import pbouda.jeffrey.manager.*;
import pbouda.jeffrey.manager.action.ProfilePostCreateActionImpl;
import pbouda.jeffrey.repository.*;
import pbouda.jeffrey.viewer.TreeTableEventViewerGenerator;

import java.nio.file.Path;

@Configuration
public class AppConfiguration {

    @Bean
    public JdbcTemplateFactory jdbcTemplateFactory(WorkingDirs workingDirs) {
        return new JdbcTemplateFactory(workingDirs);
    }

    @Bean
    public RecordingManager recordingRepository(WorkingDirs workingDirs) {
        return new FileBasedRecordingManager(workingDirs, new RecordingRepository(workingDirs));
    }

    @Bean
    public HeatmapManager.Factory heatmapFactory(WorkingDirs workingDirs, JdbcTemplateFactory jdbcTemplateFactory) {
        return profileInfo -> new DbBasedHeatmapManager(
                profileInfo, workingDirs, new HeatmapRepository(jdbcTemplateFactory.create(profileInfo)), new HeatmapGeneratorImpl());
    }

    @Bean
    public TimeseriesManager.Factory timeseriesFactory(WorkingDirs workingDirs) {
        return profileInfo -> new AdhocTimeseriesManager(
                profileInfo, workingDirs, new TimeseriesGeneratorImpl());
    }

    @Bean
    public EventViewerManager.Factory eventViewerManager(WorkingDirs workingDirs, JdbcTemplateFactory jdbcTemplateFactory) {
        return profileInfo -> new DbBasedViewerManager(
                workingDirs.profileRecording(profileInfo),
                new CacheRepository(jdbcTemplateFactory.create(profileInfo)),
                new TreeTableEventViewerGenerator());
    }

    @Bean
    public GraphManager.FlamegraphFactory flamegraphFactory(WorkingDirs workingDirs, JdbcTemplateFactory jdbcTemplateFactory) {
        return profileInfo -> new DbBasedFlamegraphManager(
                profileInfo,
                workingDirs,
                new GraphRepository(jdbcTemplateFactory.create(profileInfo), GraphType.PRIMARY),
                new FlamegraphGeneratorImpl(),
                new GraphExporterImpl(),
                new TimeseriesGeneratorImpl()
        );
    }

    @Bean
    public GraphManager.DiffgraphFactory diffgraphFactory(WorkingDirs workingDirs, JdbcTemplateFactory jdbcTemplateFactory) {
        return (primary, secondary) -> new DbBasedDiffgraphManager(
                primary,
                secondary,
                workingDirs,
                new GraphRepository(jdbcTemplateFactory.create(primary), GraphType.DIFFERENTIAL),
                new DiffgraphGeneratorImpl(),
                new GraphExporterImpl(),
                new TimeseriesGeneratorImpl()
        );
    }

    @Bean
    public ProfileManager.Factory profileManager(
            WorkingDirs workingDirs,
            JdbcTemplateFactory jdbcTemplateFactory,
            GraphManager.FlamegraphFactory flamegraphFactory,
            GraphManager.DiffgraphFactory diffgraphFactory,
            HeatmapManager.Factory heatmapFactory,
            TimeseriesManager.Factory timeseriesFactory,
            EventViewerManager.Factory eventViewerManager) {

        return profileInfo -> {
            CacheRepository cacheRepository = new CacheRepository(jdbcTemplateFactory.create(profileInfo));

            return new DbBasedProfileManager(
                    profileInfo,
                    workingDirs,
                    flamegraphFactory,
                    diffgraphFactory,
                    heatmapFactory,
                    timeseriesFactory,
                    eventViewerManager,
                    new DbBasedProfileInfoManager(profileInfo, workingDirs, cacheRepository),
                    new PersistedProfileAutoAnalysisManager(workingDirs.profileRecording(profileInfo), cacheRepository));
        };
    }

    @Bean
    public WorkingDirs jeffreyDir(
            @Value("${jeffrey.dir.home}") String homeDir,
            @Value("${jeffrey.dir.recordings}") String recordingsDir,
            @Value("${jeffrey.dir.workspace}") String workspaceDir) {

        return new WorkingDirs(Path.of(homeDir), Path.of(recordingsDir), Path.of(workspaceDir));
    }

    @Bean
    public ProfilesManager profilesManager(ProfileManager.Factory profileFactory, WorkingDirs workingDirs) {
        return new DbBasedProfilesManager(profileFactory, workingDirs, new ProfilePostCreateActionImpl());
    }
}
