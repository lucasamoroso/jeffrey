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

package pbouda.jeffrey.frameir.frame;

import jdk.jfr.consumer.RecordedFrame;
import pbouda.jeffrey.common.RecordedClassMapper;
import pbouda.jeffrey.common.Type;
import pbouda.jeffrey.frameir.FrameType;
import pbouda.jeffrey.frameir.record.AllocationRecord;

import java.util.List;

public class AllocationTopFrameProcessor extends SingleFrameProcessor<AllocationRecord> {

    @Override
    public NewFrame processSingle(AllocationRecord record, RecordedFrame currFrame, boolean topFrame) {
        FrameType currentFrameType;
        if (Type.OBJECT_ALLOCATION_IN_NEW_TLAB.sameAs(record.eventType())) {
            currentFrameType = FrameType.ALLOCATED_OBJECT_IN_NEW_TLAB_SYNTHETIC;
        } else if (Type.OBJECT_ALLOCATION_OUTSIDE_TLAB.sameAs(record.eventType())) {
            currentFrameType = FrameType.ALLOCATED_OBJECT_OUTSIDE_TLAB_SYNTHETIC;
        } else {
            currentFrameType = FrameType.ALLOCATED_OBJECT_SYNTHETIC;
        }

        return new NewFrame(
                RecordedClassMapper.map(record.allocatedClass()),
                currFrame.getLineNumber(),
                currFrame.getBytecodeIndex(),
                currentFrameType,
                true,
                record.sampleWeight());
    }

    @Override
    public boolean isApplicable(AllocationRecord record, List<RecordedFrame> stacktrace, int currIndex) {
        return currIndex == (stacktrace.size() - 1);
    }
}
