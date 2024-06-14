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

package pbouda.jeffrey.repository;

import pbouda.jeffrey.WorkingDirs;
import pbouda.jeffrey.repository.model.Recording;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Stream;

public class RecordingRepository {

    private final WorkingDirs workingDirs;

    public RecordingRepository(WorkingDirs workingDirs) {
        this.workingDirs = workingDirs;
    }

    public List<Recording> all() {
        try (Stream<Path> paths = Files.list(workingDirs.recordingsDir())) {
            return paths.filter(p -> p.getFileName().toString().endsWith(".jfr"))
                    .map(RecordingRepository::toProfile)
                    .toList();

        } catch (IOException e) {
            throw new RuntimeException("Cannot read profiles: " + workingDirs.recordingsDir(), e);
        }
    }

    private static Recording toProfile(Path file) {
        try {
            Instant modificationTime = Files.getLastModifiedTime(file).toInstant();
            long sizeInBytes = Files.size(file);

            return new Recording(file.getFileName().toString(), toDateTime(modificationTime), sizeInBytes);
        } catch (IOException e) {
            throw new RuntimeException("Cannot get info about profile: " + file, e);
        }
    }

    private static LocalDateTime toDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.SECONDS);
    }
}
