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

package pbouda.jeffrey.frameir.processor;

import jdk.jfr.consumer.RecordedEvent;
import pbouda.jeffrey.common.AbsoluteTimeRange;
import pbouda.jeffrey.common.Type;
import pbouda.jeffrey.frameir.record.ExecutionSampleRecord;
import pbouda.jeffrey.frameir.record.StackBasedRecord;
import pbouda.jeffrey.frameir.tree.SimpleTreeBuilder;

import java.time.Instant;
import java.util.List;

public class SimpleEventProcessor extends StacktraceBasedEventProcessor<StackBasedRecord> {

    public SimpleEventProcessor(Type eventType, AbsoluteTimeRange absoluteTimeRange, boolean threadMode) {
        super(List.of(eventType), absoluteTimeRange, new SimpleTreeBuilder(threadMode));
    }

    public SimpleEventProcessor(
            List<Type> eventTypes,
            AbsoluteTimeRange absoluteTimeRange,
            SimpleTreeBuilder treeBuilder) {
        super(eventTypes, absoluteTimeRange, treeBuilder);
    }

    @Override
    protected ExecutionSampleRecord mapEvent(RecordedEvent event, Instant modifiedEventTime) {
        if (event.hasField("sampledThread")) {
            return new ExecutionSampleRecord(
                    modifiedEventTime,
                    event.getStackTrace(),
                    event.getThread("sampledThread"));
        } else {
            return new ExecutionSampleRecord(
                    modifiedEventTime,
                    event.getStackTrace(),
                    null);
        }
    }
}
