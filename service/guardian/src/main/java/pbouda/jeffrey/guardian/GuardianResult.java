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

package pbouda.jeffrey.guardian;

import pbouda.jeffrey.frameir.Frame;
import pbouda.jeffrey.guardian.guard.GuardAnalysisResult;

public record GuardianResult(GuardAnalysisResult analysisItem, Frame frame) {

    public static GuardianResult of(GuardAnalysisResult analysisItem) {
        return new GuardianResult(analysisItem, null);
    }

    public static GuardianResult of(GuardAnalysisResult analysisItem, Frame frame) {
        return new GuardianResult(analysisItem, frame);
    }

    /**
     * Whether the evaluation of ended up with Warning and contains a frame. Some guards might not have a frame
     * as well (e.g. {@link pbouda.jeffrey.guardian.guard.TotalSamplesGuard}).
     *
     * @return true if the result contains a frame.
     */
    public boolean containsFrame() {
        return frame != null;
    }
}
