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

import java.util.List;

public abstract class LambdaMatchUtils {

    public static final String DIRECT_METHOD_HANDLE_HOLDER_CLASS = "java.lang.invoke.DirectMethodHandle$Holder";
    public static final String LAMBDA_FORM_CLASS = "java.lang.invoke.LambdaForm$";
    public static final String LAMBDA_METHOD = "lambda$";

    public static boolean matchLambdaFrames(List<RecordedFrame> stacktrace, int currIndex) {
        RecordedFrame currFrame = stacktrace.get(currIndex);
        return isLambdaForm(currFrame)
                || isDirectMethodHandle(currFrame)
                || isLambdaClass(currFrame)
                || isLambdaMethod(currFrame);
    }

    /**
     * Parsing of:
     * ch.qos.logback.classic.joran.JoranConfigurator$$Lambda.0x00007fc6071135c0
     */
    private static boolean isLambdaClass(RecordedFrame frame) {
        String clazz = frame.getMethod().getType().getName();
        return clazz.contains("$$Lambda");
    }

    private static boolean isLambdaForm(RecordedFrame frame) {
        return frame.getMethod().getType().getName().startsWith(LAMBDA_FORM_CLASS);
    }

    private static boolean isDirectMethodHandle(RecordedFrame frame) {
        return frame.getMethod().getType().getName().startsWith(DIRECT_METHOD_HANDLE_HOLDER_CLASS);
    }

    private static boolean isLambdaMethod(RecordedFrame frame) {
        return frame.getMethod().getName().startsWith(LAMBDA_METHOD);
    }

    private static boolean isNextLambdaMethod(List<RecordedFrame> stacktrace, int currIndex) {
        int nextIndex = currIndex + 1;
        if (nextIndex >= stacktrace.size()) {
            return false;
        }
        return isLambdaMethod(stacktrace.get(nextIndex));
    }
}
