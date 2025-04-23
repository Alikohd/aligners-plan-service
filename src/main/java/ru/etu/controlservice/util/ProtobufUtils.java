package ru.etu.controlservice.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;

import java.util.List;

public class ProtobufUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<Struct> jsonNodesToStructs(List<JsonNode> jsonNodes) {
        return jsonNodes.stream()
                .map(element -> {
                    try {
                        Struct.Builder builder = Struct.newBuilder();
                        JsonFormat.parser().merge(element.toString(), builder);
                        return builder.build();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to convert JsonNode to Struct", e);
                    }
                })
                .toList();
    }

    public static List<JsonNode> structsToJsonNodes(List<Struct> structs) {
        return structs.stream()
                .map(struct -> {
                    try {
                        String jsonString = JsonFormat.printer().print(struct);
                        return mapper.readTree(jsonString);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to convert Struct to JsonNode", e);
                    }
                })
                .toList();
    }
}
