package pbouda.jeffrey.graph.diff;

import com.fasterxml.jackson.databind.node.ObjectNode;
import pbouda.jeffrey.common.Config;

public interface DiffgraphGenerator {

    ObjectNode generate(Config config);

}
