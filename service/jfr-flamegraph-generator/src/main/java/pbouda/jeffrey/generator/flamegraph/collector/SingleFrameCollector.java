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

package pbouda.jeffrey.generator.flamegraph.collector;

import pbouda.jeffrey.generator.flamegraph.Frame;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class SingleFrameCollector<OUTPUT> implements Collector<Frame, AtomicReference<Frame>, OUTPUT> {

    private final Function<Frame, OUTPUT> graphBuilder;

    public SingleFrameCollector(Function<Frame, OUTPUT> graphBuilder) {
        this.graphBuilder = graphBuilder;
    }

    @Override
    public Supplier<AtomicReference<Frame>> supplier() {
        return AtomicReference::new;
    }

    @Override
    public BiConsumer<AtomicReference<Frame>, Frame> accumulator() {
        return AtomicReference::set;
    }

    @Override
    public BinaryOperator<AtomicReference<Frame>> combiner() {
        return (first, second) -> first;
    }

    @Override
    public Function<AtomicReference<Frame>, OUTPUT> finisher() {
        return atomic -> {
            Frame frame = atomic.get();
            return graphBuilder.apply(frame);
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }
}
