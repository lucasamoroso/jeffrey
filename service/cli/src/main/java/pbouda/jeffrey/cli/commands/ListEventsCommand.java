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

package pbouda.jeffrey.cli.commands;

import pbouda.jeffrey.cli.CliUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;

@Command(
        name = "events",
        description = "List all event-types containing a stacktrace for building a flamegraph",
        mixinStandardHelpOptions = true)
public class ListEventsCommand implements Runnable {

    @Parameters(paramLabel = "<jfr_file>", description = "one JFR file for listing stack-based events", arity = "1")
    File file;

    @Override
    public void run() {
        try {
            CliUtils.listStackBasedEventTypes(file.toPath())
                    .forEach(type -> System.out.println(type.getName() + " (" + type.getLabel() + ")"));
        } catch (Exception e) {
            System.out.println("Cannot read events: file=" + file + " error=" + e.getMessage());
        }
    }
}
