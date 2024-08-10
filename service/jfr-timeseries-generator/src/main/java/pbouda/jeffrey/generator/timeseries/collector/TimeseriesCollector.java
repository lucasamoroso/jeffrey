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

package pbouda.jeffrey.generator.timeseries.collector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.eclipse.collections.impl.map.mutable.primitive.LongLongHashMap;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class TimeseriesCollector implements Collector<LongLongHashMap, LongLongHashMap, ArrayNode> {

    @Override
    public Supplier<LongLongHashMap> supplier() {
        return LongLongHashMap::new;
    }

    @Override
    public BiConsumer<LongLongHashMap, LongLongHashMap> accumulator() {
        return (left, right) -> {
            right.forEachKeyValue(left::addToValue);
        };
    }

    @Override
    public BinaryOperator<LongLongHashMap> combiner() {
        // Iterate the smaller map and add all values to the bigger one
        return (left, right) -> {
            if (left.size() > right.size()) {
                right.forEachKeyValue(left::addToValue);
                return left;
            } else {
                left.forEachKeyValue(right::addToValue);
                return right;
            }
        };
    }

    @Override
    public Function<LongLongHashMap, ArrayNode> finisher() {
        return TimeseriesCollectorUtils::buildTimeseries;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }
}
